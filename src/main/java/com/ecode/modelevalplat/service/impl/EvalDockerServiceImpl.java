package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.service.CompetitionService;
import com.ecode.modelevalplat.service.EvalDockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class EvalDockerServiceImpl implements EvalDockerService {
    private static final Logger logger = LoggerFactory.getLogger(EvalDockerServiceImpl.class);

    @Autowired
    private CompetitionService competitionService;

    @Override
    public void executeDocker(Long id) {
        String customSubdir = competitionService.selectPath(id);

        try {
            // 1. 构建Docker镜像
            Process buildProcess = new ProcessBuilder(
                    "docker", "build", "-t", "my-predict-app",
                    "-f", "/mnt/d/CZY/ModelEvalPlat/Dockerfile", "."
            ).redirectErrorStream(true).start(); // 合并错误流和输出流
            // 实时捕获构建输出
            logProcessOutput(buildProcess, "BUILD");
            int buildExitCode = buildProcess.waitFor();

            if (buildExitCode != 0) {
                logger.error("❌ Docker构建失败! 退出码: {}", buildExitCode);
                return;
            }

            logger.info("✅ Docker构建成功!");

            // 2. 运行Docker容器
            Process runProcess = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", "/mnt/d/CZY/ModelEvalPlat:/app/data", // 使用绝对路径
                    "-e", "DATA_DIR=/app/data/" + customSubdir,
                    "my-predict-app"
            ).redirectErrorStream(true).start();

            // 实时捕获运行输出
            logProcessOutput(runProcess, "RUN");
            int runExitCode = runProcess.waitFor();

            if (runExitCode != 0) {
                logger.error("❌ Docker运行失败! 退出码: {}", runExitCode);
            } else {
                logger.info("✅ Docker运行成功!");
            }

        } catch (IOException | InterruptedException e) {
            logger.error("🚨 Docker调用异常", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }

    // 实时打印进程输出
    private void logProcessOutput(Process process, String prefix) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[{}] {}", prefix, line);
                }
            } catch (IOException e) {
                logger.error("输出流读取失败", e);
            }
        }).start();
    }
}