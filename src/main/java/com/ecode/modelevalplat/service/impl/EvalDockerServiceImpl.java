package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.service.CompetitionService;
import com.ecode.modelevalplat.service.EvalDockerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class EvalDockerServiceImpl implements EvalDockerService {
    private static final Logger logger = LoggerFactory.getLogger(EvalDockerServiceImpl.class);

    @Autowired
    private CompetitionService competitionService;

    private DockerClient dockerClient;

    @PostConstruct
    public void init() {
        // 配置Docker客户端连接
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    @Override
    public void executeDocker(String datasetPath, Path targetDir, Long id, Long submissionId) {
        // 1. 构建Docker镜像
        String imageName = "model-evaluator-" + submissionId;

        File buildContext = targetDir.toFile(); // 使用 targetDir 作为构建上下文
        File dockerfile = new File(buildContext, "Dockerfile"); // Dockerfile 在构建上下文内
        System.out.println("这是："+dockerfile.getPath());
        BuildImageResultCallback buildCallback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                String stream = item.getStream();
                if (stream != null && !stream.trim().isEmpty()) {
                    logger.info("[BUILD] {}", stream.trim());
                }
                super.onNext(item);
            }
        };

        try {
            String imageId = dockerClient.buildImageCmd(buildContext)
                    .withDockerfile(dockerfile)
                    .withTags(Collections.singleton(imageName))
                    .exec(buildCallback)
                    .awaitImageId();

            logger.info("✅ Docker构建成功! Image ID: {}", imageId);

            // 2. 准备容器挂载卷
            Path resultDir = targetDir.resolve("prediction_result");
            Bind hostDataBind = new Bind(
                    "/mnt/d/CZY/ModelEvalPlat",
//                    targetDir.toString(),
                    new Volume("/app/data")
            );
            Bind resultDirBind = new Bind(
                    resultDir.toString(),
                    new Volume("/app/prediction_result")
            );

            // 3. 创建并启动容器
            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withEnv("DATA_DIR=/app/data/" + datasetPath)
                    .withHostConfig(new HostConfig()
//                            .withBinds( resultDirBind)
                            .withBinds(hostDataBind, resultDirBind)
                            .withAutoRemove(true) // 设置容器自动删除
                    )
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            logger.info("▶️ 启动容器: {}", container.getId());

            // 4. 捕获容器日志
            dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            logger.info("[RUN] {}", new String(frame.getPayload()).trim());
                        }
                    });

            // 5. 等待容器执行完成
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            Integer exitCode = dockerClient.waitContainerCmd(container.getId())
                    .exec(waitCallback)
                    .awaitStatusCode();

            if (exitCode != 0) {
                logger.error("❌ Docker运行失败! 退出码: {}", exitCode);
            } else {
                logger.info("✅ Docker运行成功!");
            }

        } catch (Exception e) {
            logger.error("🚨 Docker操作异常", e);
        } finally {
            // 清理镜像
            try {
                dockerClient.removeImageCmd(imageName).exec();
                logger.info("🧹 已清理Docker镜像: {}", imageName);
            } catch (Exception e) {
                logger.error("⚠️ 清理镜像失败: {}", e.getMessage());
            }
        }
    }
}