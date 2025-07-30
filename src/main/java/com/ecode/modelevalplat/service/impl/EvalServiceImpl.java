package com.ecode.modelevalplat.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.common.enums.EvalStatusEnum;

import java.io.*;
import java.nio.file.*;


import com.ecode.modelevalplat.common.exception.PythonExecuteException;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.service.EvalService;
import com.ecode.modelevalplat.dao.mapper.EvaluationResultMapper;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.util.FileSystemUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvalServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResultDO> implements EvalService {


    private final SubmissionMapper submissionMapper;
    private final BaseMapper<EvaluationResultDO> evaluationResultMapper;
    private final CompetitionMapper competitionMapper;

    // 线程池配置
    private final ThreadPoolExecutor evalExecutor = new ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    );
    // 任务队列
//    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    @Override
    public void evaluateModel(Long submissionId) {
        Long competitionId = submissionMapper.getCompetitionId(submissionId);

        Path targetDir = null;
        String evalStatus = EvalStatusEnum.PROCESSING.name();
        submissionMapper.updateStatusById(submissionId, evalStatus);
        log.info("开始评估模型: {}", submissionId);
        EvaluationResultDO evaluationResult = new EvaluationResultDO();
        try {
            // 1.获取提交文件地址
            String modelPath = submissionMapper.getModelPath(submissionId);
            if (modelPath == null || modelPath.isEmpty()) {
                throw new RuntimeException("模型路径不存在 for submission: " + submissionId);
            }

            // 2. 解压ZIP文件
            Path originalZipPath = Paths.get(modelPath);
            // 生成解压目录路径targetDir（与ZIP文件同名不带扩展名）
            String targetDirName = originalZipPath.getFileName().toString().replace(".zip", "");
            targetDir = originalZipPath.getParent().resolve(targetDirName);
            unzipModelFile(modelPath, targetDir);

            // 3. 获取测试集路径
            String datasetPath = Paths.get(competitionMapper.getPath(competitionId)).resolve("data").toString();

            // 4. 运行Python脚本，并根据是否成功运行创建评估记录
            // TODO 检查submitType，如果是DOCKER，则执行Docker脚本；如果是MODEL，则构建DOCKERFILE后执行Docker脚本
            executePythonScript(datasetPath, targetDir);

            // 5. 如果运行成功，根据预测结果csv计算得分
            String predictCsvPath = targetDir.resolve("prediction_result").resolve("result.csv").toString();
            String groundTruthCsvPath = Paths.get(competitionMapper.getPath(competitionId)).resolve("Ground_Truth.csv").toString();
            evaluationResult = processEvaluationResult(predictCsvPath, groundTruthCsvPath, submissionId);

            // 评估执行完毕，无异常，更新提交状态为SUCCESS
            evalStatus = EvalStatusEnum.SUCCESS.name();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (PythonExecuteException e) {
            throw new PythonExecuteException("python进程错误：" + e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("评估模块出错：" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("未知错误：" + e.getMessage());
        } finally {
            // 如评估成功则保存评估结果
            if (evalStatus.equals(EvalStatusEnum.SUCCESS.name())) {
                evaluationResultMapper.insert(evaluationResult);
            } else {
                evalStatus = EvalStatusEnum.FAILED.name();
            }

            // 更新提交状态
            submissionMapper.updateStatusById(submissionId, evalStatus);

            // 删除解压的临时文件
            try {
                if (targetDir != null) {
                    FileSystemUtils.deleteRecursively(targetDir);
                }
            } catch (IOException e) {
                log.error("删除临时文件失败: {} {}", e, e.getMessage());
            }
            log.info("评估结束: {}", submissionId);
        }
    }

    private void unzipModelFile(String modelPath, Path targetDir) throws IOException {
        try {
            // 创建目标解压目录
            Files.createDirectories(targetDir);

            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(modelPath))) {
                ZipEntry entry;

                while ((entry = zipIn.getNextEntry()) != null) {
                    Path entryPath = targetDir.resolve(entry.getName()).normalize();

                    // 安全校验：确保解压路径在目标目录内
                    if (!entryPath.startsWith(targetDir)) {
                        throw new IOException("非法文件路径: " + entry.getName());
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zipIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            log.error("解压失败", e);
            throw new IOException("模型文件解压失败" + e.getMessage(), e);
        }
    }

    // 生成Dockerfile
    private void generateDockerfile(Path targetDir) throws IOException {
        // TODO 读取requirements.txt中的python版本
        List<String> dockerfileContent = List.of(
                "FROM python:3.8-slim",
                "WORKDIR /app",
                "COPY . .",
                "RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple",
                "CMD [\"python\", \"code/predict.py\", \"--testdataset_path\", \"/mnt/data\"]"
        );

        Path dockerfilePath = targetDir.resolve("Dockerfile");
        Files.write(dockerfilePath, dockerfileContent, StandardOpenOption.CREATE);
    }


    @Override
    public void executeDocker(String datasetPath, Path targetDir) throws InterruptedException, IOException {
        // TODO 检查代码，并添加指定csv文件输出位置的逻辑
        // 创建结果目录（保持与Python执行逻辑相同的路径）
        Path resultDir = targetDir.resolve("prediction_result");
//        Files.createDirectories(resultDir);

        // 构建Docker镜像
        ProcessBuilder buildProcess = new ProcessBuilder(
                "docker", "build", "-t", "model-eval", "."
        );

        // 运行容器时挂载数据卷（无需复制数据集）
        ProcessBuilder runProcess = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", datasetPath + ":/mnt/data",          // 挂载测试数据集
                "-v", targetDir + ":/app",  // 宿主机解压目录映射到容器工作目录
                "-v", resultDir + ":/app/prediction_result", // 挂载结果目录
                "model-eval"
        );
        executeProcessWithLogging(buildProcess, "Docker构建");
        executeProcessWithLogging(runProcess, "Docker运行");
    }

    // 通用的进程执行方法（重构原有Python执行逻辑）
    private void executeProcessWithLogging(ProcessBuilder processBuilder, String processName)
            throws IOException, InterruptedException {

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("{} 输出: {}", processName, line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new PythonExecuteException(processName + "失败，退出码: " + exitCode);
        }
    }

    private void executePythonScript(String datasetPath, Path targetDir) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "code/predict.py",
                "--testdataset_path", datasetPath); // 传递测试集路径给Python脚本
        processBuilder.directory(new File(targetDir.toString()));

        int exitCode = -1;
        BufferedReader errorReader = null;
        Process process = null;
        try {
            process = processBuilder.start();
            // 后台读取错误流，不可返回到前端
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("Python脚本错误: {}", errorLine);
            }
            exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new PythonExecuteException("Python脚本执行失败，退出码: " + exitCode);
            }
        } finally {
            if (errorReader != null) {
                try {
                    errorReader.close();
                } catch (IOException e) {
                    log.error("Failed to close errorReader: " + e.getMessage());
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public EvaluationResultDO processEvaluationResult(String predictCsvPath, String groundTruthCsvPath, Long submissionId) throws IOException{
        // 读取 CSV 文件
        List<String> predictedLabels;
        List<String> trueLabels;
        try {
            predictedLabels = readLabelsFromCsv(predictCsvPath);
            trueLabels = readLabelsFromCsv(groundTruthCsvPath);
        } catch (IOException | CsvException e) {
            throw new RuntimeException("csv文件错误：" + e.getMessage());
        }


        // 计算评估指标
        double accuracy = calculateAccuracy(predictedLabels, trueLabels);
        List<Double> PrecisionRecallF1Score = calculatePrecisionRecallF1Score(predictedLabels, trueLabels);
        double precision = PrecisionRecallF1Score.get(0);
        double recall = PrecisionRecallF1Score.get(1);
        double f1Score = PrecisionRecallF1Score.get(2);

        // 构建评估结果的新条目，但暂不存入数据库
        SubmissionDO submission = submissionMapper.findById(submissionId);
        EvaluationResultDO evaluationResult = new EvaluationResultDO();
        evaluationResult.setUserId(submission.getUserId());
        evaluationResult.setCompetitionId(submission.getCompetitionId());
        evaluationResult.setSubmitTime(submission.getSubmitTime());
        // TODO:根据不同比赛的要求计算分数并存储
        double score = 0.5 * accuracy + 0.5 * f1Score;
        evaluationResult.setScore((float) score);
        // 可以扩展将准确率等其他指标存入 resultJson
        evaluationResult.setResultJson(String.format("{\"accuracy\": %.2f, \"precision\": %.2f, \"recall\": %.2f, \"f1Score\": %.2f}", accuracy, precision, recall, f1Score));
        return evaluationResult;
    }

    private List<String> readLabelsFromCsv(String csvPath) throws IOException, CsvException {
        List<String> labels = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                // 假设标签在第一列，根据实际情况调整
                labels.add(row[0]);
            }
        }
        return labels;
    }

    private double calculateAccuracy(List<String> predicted, List<String> actual) {
        long correct = IntStream.range(0, predicted.size())
                .filter(i -> predicted.get(i).equals(actual.get(i)))
                .count();
        return (double) correct / predicted.size();
    }

    private List<Double> calculatePrecisionRecallF1Score(List<String> predicted, List<String> actual) {
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (int i = 0; i < predicted.size(); i++) {
            if (predicted.get(i).equals(actual.get(i))) {
                truePositives++;
            } else {
                falsePositives++;
                falseNegatives++;
            }
        }

        double precision = (double) truePositives / (truePositives + falsePositives);
        double recall = (double) truePositives / (truePositives + falseNegatives);
        double f1Score = 2 * (precision * recall) / (precision + recall);

        return List.of(precision, recall, f1Score);
    }

    @Override
    public void asyncEvaluateModel(Long submissionId, String submitType) {
        // 将任务加入队列
        evalExecutor.execute(() -> {
            try {
                // 实际评估逻辑
                if (submitType.equals("MODEL")) {
                    this.evaluateModel(submissionId);
                }
//                else if (submitType.equals("DOCKER")) {
//                    this.evaluateDocker(submissionId);
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
