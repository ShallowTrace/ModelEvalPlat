import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import java.io.File;

import java.util.Set;

import com.github.dockerjava.api.model.Frame;

public class test {

    @Test
    public void testDockerOperations() throws Exception {
        // Docker 客户端配置
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        // 参数定义
        String submissionId = "12345";
        String datasetPath = "dataset";
        String resultDir = "/mnt/d/CZY/ModelEvalPlat/results";

        try {
            // 1. 构建镜像
            String imageName = "model-evaluator-" + submissionId;
            File dockerfile = new File("/mnt/d/CZY/ModelEvalPlat/Dockerfile");

            String imageId = dockerClient.buildImageCmd()
                    .withDockerfile(dockerfile)
                    .withBaseDirectory(dockerfile.getParentFile()) // 设置构建上下文
                    .withTags(Set.of(imageName))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();

            // 2. 运行容器
            HostConfig hostConfig = new HostConfig()
                    .withBinds(
                            new Bind("/mnt/d/CZY/ModelEvalPlat", new Volume("/app/data")),
                            new Bind(resultDir, new Volume("/app/prediction_result"))
                    )
                    .withAutoRemove(true); // 对应 --rm 参数

            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(hostConfig)
                    .withEnv("DATA_DIR=/app/data/" + datasetPath)
                    .exec();

            // 启动容器
            dockerClient.startContainerCmd(container.getId()).exec();

            // 等待容器完成并获取退出码
            Integer exitCode = dockerClient.waitContainerCmd(container.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode();

            // 验证退出码
            if (exitCode != 0) {
                // 获取容器日志
                LogContainerCmd logCmd = dockerClient.logContainerCmd(container.getId())
                        .withStdOut(true)
                        .withStdErr(true);
                String logs = logCmd.exec(new LoggingCallback()).toString();
                throw new RuntimeException("Container failed with exit code " + exitCode + ". Logs: " + logs);
            }

        } finally {
            // 清理资源（可选）
            try {
                dockerClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 在测试类内部定义日志回调
    private static class LoggingCallback extends ResultCallback.Adapter<Frame> {
        private final StringBuilder log = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            // 将字节流转换为字符串（区分 stdout/stderr）
            String text;
            try {
                text = IOUtils.toString(frame.getPayload(), "UTF-8");
            } catch (Exception e) {
                text = "[LOG PARSE ERROR] " + e.getMessage();
            }

            // 追加日志内容
            log.append("hello world")
                    ;
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }

}