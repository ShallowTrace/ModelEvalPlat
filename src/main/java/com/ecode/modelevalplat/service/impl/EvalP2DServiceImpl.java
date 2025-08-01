package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.service.EvalP2DService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;


// 用于解析环境配置文件，生成对应的Dockerfile
@Service
@Slf4j
public class EvalP2DServiceImpl implements EvalP2DService {

//    public static void main(String[] args) throws IOException {
//        generateDockerfile(Paths.get("C:\\Users\\ShallowTrace\\Desktop\\工行实习\\用户上传文件_分类任务测试样例1\\team1"));
//    }
    @Override
    public void generateDockerfile(Path projectPath) throws IOException {
        // 读取环境配置
        EnvironmentConfig config = readEnvironmentConfig(projectPath);

        // 生成Dockerfile内容
        List<String> dockerfileLines = new ArrayList<>();
        addBaseImage(config, dockerfileLines);
        addPythonBase(dockerfileLines, config);
        addSystemDependencies(projectPath, config, dockerfileLines);
        addPythonEnvironment(config, dockerfileLines);
        addPythonDependencies(projectPath, config, dockerfileLines);
        addAppFiles(config, dockerfileLines);
        addEntryPoint(config, dockerfileLines);

        // 写入Dockerfile
        Files.write(projectPath.resolve("Dockerfile"),
                dockerfileLines,
                StandardOpenOption.CREATE);
    }

    public EnvironmentConfig readEnvironmentConfig(Path projectPath) throws IOException {
        Path configPath = projectPath.resolve("environment.json");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configPath.toFile(), EnvironmentConfig.class);
    }

    private static void addBaseImage(EnvironmentConfig config, List<String> lines) {
        if ("cuda".equals(config.getHardware().getType())) {
            String cudaVersion = normalizeCudaVersion(config.getHardware().getCudaVersion());
            lines.add("FROM nvidia/cuda:" + cudaVersion + "-runtime-ubuntu22.04");
        } else {
            lines.add("FROM python:" + normalizePythonVersion(config.getPython().getVersion()) + "-slim-bullseye");
        }
        lines.add("");
    }

    // cuda的docker版本号规范化方法
    private static String normalizeCudaVersion(String version) {
        String[] parts = version.split("\\.");
        List<String> normalized = new ArrayList<>(Arrays.asList(parts));

        // 补全版本号到三位 (major.minor.patch)
        while (normalized.size() < 3) {
            normalized.add("0");
        }

        // 截断超过三位的版本号
        return String.join(".", normalized.subList(0, 3));
    }

    private static String normalizePythonVersion(String version) {
        String[] parts = version.split("\\.");
        List<String> normalized = new ArrayList<>(Arrays.asList(parts));

        // 补全版本号到两位
        if (normalized.size() == 1) {
            normalized.add("6"); // 默认补全为.x.6版本
        }

        // 截断到两位
        return String.join(".", normalized.subList(0, Math.min(2, normalized.size())));
    }

    private static void addPythonBase(List<String> lines, EnvironmentConfig config) {
        lines.add("# 安装Python基础环境");
        if ("cuda".equals(config.getHardware().getType())) {
            // CUDA镜像需要安装指定版本Python
            String pythonVersion = normalizePythonVersion(config.getPython().getVersion());
            lines.add("RUN apt-get update && \\");
            lines.add("    apt-get install -y --no-install-recommends \\");
            lines.add("    software-properties-common \\");
            lines.add("    && add-apt-repository -y ppa:deadsnakes/ppa \\");
            lines.add("    && apt-get update \\");
            lines.add("    && apt-get install -y python" + pythonVersion + " \\");
            lines.add("    python" + pythonVersion + "-distutils \\");  // distutils安装
            lines.add("    python3-pip \\");  // pip安装
            lines.add("    python3-setuptools \\");  // setuptools
            lines.add("    python3-wheel \\");  // wheel
            lines.add("    && rm -rf /var/lib/apt/lists/*");
            String pyCommand = "python" + pythonVersion;
            lines.add("RUN ln -sf /usr/bin/" + pyCommand + " /usr/bin/python && \\");
            lines.add("    ln -sf /usr/bin/pip3 /usr/bin/pip");
            lines.add("");
        } else {
            // 非CUDA版本的基础镜像使用的python，已经默认安装
            lines.add("RUN apt-get update && \\");
            lines.add("    apt-get install -y --no-install-recommends \\");
            lines.add("    python3-pip \\");
            lines.add("    python3-setuptools \\");
            lines.add("    python3-wheel \\");
            lines.add("    && rm -rf /var/lib/apt/lists/*");
            lines.add("RUN ln -sf /usr/bin/python3 /usr/bin/python && \\");
            lines.add("    ln -sf /usr/bin/pip3 /usr/bin/pip");
            lines.add("");
        }
    }

    private static void addSystemDependencies(Path projectPath, EnvironmentConfig config, List<String> lines) {
        String sysDepFile = config.getDependencies().getSystem();
        if (sysDepFile != null) {
            Path sysDepPath = projectPath.resolve(sysDepFile);
            if (Files.exists(sysDepPath)) {
                lines.add("# 安装系统依赖");
                lines.add("RUN apt-get update && apt-get install -y --no-install-recommends \\");
                try {
                    List<String> deps = Files.readAllLines(sysDepPath);
                    deps.removeIf(String::isEmpty); // 移除空行
                    for (int i = 0; i < deps.size(); i++) {
                        String line = "    " + deps.get(i).trim();
                        if (i < deps.size() - 1) line += " \\";
                        lines.add(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("读取系统依赖文件失败", e);
                }
                lines.add("    && rm -rf /var/lib/apt/lists/*");
                lines.add("");
            }
        }
    }

    private static void addPythonEnvironment(EnvironmentConfig config, List<String> lines) {
        // 设置环境变量
        config.getEnvironmentVariables().forEach((k, v) ->
                lines.add("ENV " + k + "=" + v));

        lines.add("WORKDIR /app");
        lines.add("");
    }

    private static void addPythonDependencies(Path projectPath, EnvironmentConfig config, List<String> lines) {
        String pyDepFile = config.getDependencies().getPython();
        if (pyDepFile != null) {
            Path pyDepPath = projectPath.resolve(pyDepFile);
            if (Files.exists(pyDepPath)) {
                lines.add("# 安装Python依赖");
                lines.add("COPY " + pyDepFile + " .");

                // 检查是否有国内源需求
                boolean useChinaSource = shouldUseChinaSource(pyDepPath);

                if (useChinaSource) {
                    String cuda_version = config.getHardware().getCudaVersion().replace(".", "");
                    lines.add("RUN pip install --no-cache-dir -r " + pyDepFile + " \\");
                    lines.add("    --index-url https://mirrors.tuna.tsinghua.edu.cn/pytorch-wheels/cu" + cuda_version + " \\");
                    lines.add("    --extra-index-url https://mirrors.aliyun.com/pytorch-wheels/cu" + cuda_version + " \\");
                    lines.add("    --extra-index-url https://pypi.tuna.tsinghua.edu.cn/simple \\");
                    lines.add("    --extra-index-url https://download.pytorch.org/whl/cu" + cuda_version);
                } else {
                    lines.add("RUN pip install --no-cache-dir -r " + pyDepFile);
                }

                lines.add("");
            }
        }
    }

    private static boolean shouldUseChinaSource(Path requirementsPath) {
        try {
            String content = Files.readString(requirementsPath);
            // 检测是否包含需要国内加速的包
            return content.contains("torch") ||
                    content.contains("tensorflow") ||
                    content.contains("paddlepaddle");
        } catch (IOException e) {
            throw new RuntimeException("读取python依赖文件失败", e);
        }
    }

    private static void addAppFiles(EnvironmentConfig config, List<String> lines) {
        lines.add("# 复制应用文件");
        lines.add("COPY code /app/code");
        lines.add("COPY prediction_result /app/prediction_result");
        lines.add("");
    }

    private static void addEntryPoint(EnvironmentConfig config, List<String> lines) {
        lines.add("# 设置启动命令");
//        lines.add("CMD [\"sh\", \"-c\", \"python\", \"code/predict.py\", \"--testdataset_path\", \"${DATA_DIR:-/app/data}\"]");
        lines.add("CMD [\"sh\", \"-c\", \"python code/predict.py --testdataset_path data\"]");
    }

    // 配置类
    @Data
    static class EnvironmentConfig {
        private Hardware hardware;
        private Python python;
        private Dependencies dependencies;
        @JsonProperty("environment_variables")
        private Map<String, String> environmentVariables;
    }

    @Data
    static class Hardware {
        private String type; // "cpu" 或 "cuda"
        @JsonProperty("cuda_version")
        private String cudaVersion; // cuda时必须
    }

    @Data
    static class Python {
        private String version;
    }

    @Data
    static class Dependencies {
        private String python; // 指向Python依赖文件，如requirements.txt
        private String system; // 指向系统依赖文件，如system-dependencies.txt，每行一个包名
    }
}