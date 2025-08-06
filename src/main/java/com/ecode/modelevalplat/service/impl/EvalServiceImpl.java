package com.ecode.modelevalplat.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.common.enums.EvalStatusEnum;

import java.io.*;
import java.nio.file.*;


import com.ecode.modelevalplat.common.exception.DockerException;
import com.ecode.modelevalplat.common.exception.PythonExecuteException;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import com.ecode.modelevalplat.service.EvalDockerService;
import com.ecode.modelevalplat.service.EvalP2DService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final EvalP2DService evalP2DService;
    private final EvalDockerService evalDockerService;

    // 线程池配置
    private final ThreadPoolExecutor evalExecutor = new ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    );
    // 任务队列
//    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    @Override
    public void evaluateModel(Long submissionId, String submitType) {
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
            System.out.println("测试git合并");
            // 2. 解压ZIP文件
            Path originalZipPath = Paths.get(modelPath);
            // 生成解压目录路径targetDir（与ZIP文件同名不带扩展名）
            String targetDirName = originalZipPath.getFileName().toString().replace(".zip", "");
            targetDir = originalZipPath.getParent().resolve(targetDirName);
            System.out.println("modelPath:"+modelPath);
            System.out.println("targetDir:"+targetDir);
            unzipModelFile(modelPath, targetDir);

            // 3. 获取测试集路径和真实值csv路径
            // 测试集路径在数据库保存的测试集路径下的data目录下，真实值csv在数据库保存的测试集路径下的Ground_Truth.csv
            Path competitionDatasetAndCSVPath = Paths.get(competitionMapper.selectPath(competitionId));
            String datasetPath = competitionDatasetAndCSVPath.resolve("data").toString();
            String groundTruthCsvPath = competitionDatasetAndCSVPath.resolve("Ground_Truth.csv").toString();

            // 4. 检查submitType，如果是DOCKER，则执行Docker脚本；如果是MODEL，则构建DOCKERFILE后执行Docker脚本
            // executePythonScript(datasetPath, targetDir);
            if (submitType.equals("MODEL")) {
                evalP2DService.generateDockerfile(targetDir);
                executeP2DDocker(datasetPath, targetDir, submissionId);
            }
            else if (submitType.equals("DOCKER")) {
                evalDockerService.executeDocker(datasetPath,targetDir,competitionId,submissionId);
            }


            // 5. 如果运行成功，根据预测结果csv计算得分
            String predictCsvPath = targetDir.resolve("prediction_result").resolve("result.csv").toString();
            System.out.println("第一个"+predictCsvPath+"第二个"+groundTruthCsvPath);
            evaluationResult = processClassificationEvaluationResult(predictCsvPath, groundTruthCsvPath, submissionId);

            // 评估执行完毕，无异常，更新提交状态为SUCCESS
            evalStatus = EvalStatusEnum.SUCCESS.name();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
//        } catch (PythonExecuteException e) {
//            throw new PythonExecuteException("python进程错误：" + e.getMessage());
        } catch (DockerException e) {
            throw new DockerException("docker进程错误：" + e.getMessage());
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


            if (targetDir != null) {
                // 删除Docker镜像
                ProcessBuilder dockerDeleteProcess = new ProcessBuilder(
                        "docker", "rmi", "-f", "model-evaluator-" + submissionId
                );
                dockerDeleteProcess.directory(new File(targetDir.toString()));
                try {
                    executeProcessWithLogging(dockerDeleteProcess, "删除Docker镜像" + submissionId);
                } catch (IOException | InterruptedException e) {
                    log.error("删除Docker镜像失败: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("删除镜像异常: {}", e.getMessage());
                }

                // 删除解压的临时文件
//                try {
//                    FileSystemUtils.deleteRecursively(targetDir);
//                } catch (IOException e) {
//                    log.error("删除临时文件失败: {} {}", e, e.getMessage());
//                }
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


        // 清空prediction_result目录
        Path predictionResultDir = targetDir.resolve("prediction_result");
        if (Files.exists(predictionResultDir) && Files.isDirectory(predictionResultDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(predictionResultDir)) {
                if (stream.iterator().hasNext()) {
                    FileSystemUtils.deleteRecursively(predictionResultDir);
                    Files.createDirectories(predictionResultDir);
                    log.info("已清空prediction_result目录: {}", predictionResultDir);
                }
            } catch (IOException e) {
                log.error("清理prediction_result目录失败: {}", e.getMessage());
                throw new IOException("清理prediction_result目录失败: " + e.getMessage(), e);
            }

        }
        else{
            Files.createDirectories(predictionResultDir);
        }
    }

    @Override
    public void executeP2DDocker(String datasetPath, Path targetDir, Long submissionId) throws InterruptedException, IOException {
        //获取是否使用cuda，结果可能为cpu、cuda
        Path configPath = targetDir.resolve("environment.json");
        ObjectMapper mapper = new ObjectMapper();
        EvalP2DServiceImpl.EnvironmentConfig config = mapper.readValue(configPath.toFile(), EvalP2DServiceImpl.EnvironmentConfig.class);
        String hardwareType = config.getHardware().getType();

        // 获取结果目录（保持与Python执行逻辑相同的路径）
        Path resultDir = targetDir.resolve("prediction_result");
        Files.createDirectories(resultDir);

        // 构建Docker镜像
        ProcessBuilder dockerBuildProcess = new ProcessBuilder(
                "docker", "build", "--quiet", "-t", "model-evaluator-" + submissionId, "."
        );
        // docker build --quiet -t model-evaluator .
        dockerBuildProcess.directory(new File(targetDir.toString()));

        // 运行容器时挂载数据卷和用作输出的空目录
        ProcessBuilder dockeRunProcess;
        if ("cuda".equals(hardwareType)) { // 根据比赛配置判断是否需要GPU
            dockeRunProcess = new ProcessBuilder(
                    "docker", "run", "--quiet", "--rm", "--gpus", "all",
                    "-v", datasetPath + ":/app/data:ro",
                    "-v", resultDir + ":/app/prediction_result",
                    "model-evaluator-" + submissionId
            );
        } else {
            dockeRunProcess = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", datasetPath + ":/app/data:ro",
                    "-v", resultDir + ":/app/prediction_result",
                    "model-evaluator-" + submissionId
            );
        }
        // docker run --rm --gpus all -v C:\Users\ShallowTrace\Desktop\工行实习\用户上传文件_分类任务测试样例1\测试集及标签:/app/data:ro -v C:\Users\ShallowTrace\Desktop\工行实习\用户上传文件_分类任务测试样例1\team1\prediction_result:/app/prediction_result model-evaluator
        // docker run --rm -v C:\Users\ShallowTrace\Desktop\工行实习\用户上传文件_分类任务测试样例1\测试集及标签\data:/app/data:ro -v C:\Users\ShallowTrace\Desktop\工行实习\用户上传文件_分类任务测试样例1\team1\prediction_result:/app/prediction_result model-evaluator
        dockeRunProcess.directory(new File(targetDir.toString()));

        executeProcessWithLogging(dockerBuildProcess, "Docker构建" + submissionId);
        executeProcessWithLogging(dockeRunProcess, "Docker运行" + submissionId);
    }

    // ProcessBuilder的执行、输出读取、错误处理通用方法
    private void executeProcessWithLogging(ProcessBuilder processBuilder, String processName)
            throws IOException, InterruptedException {

//        processBuilder.redirectErrorStream(true);

        int exitCode = -1;
        BufferedReader errorReader = null;
        Process process = null;
        try {
            process = processBuilder.start();
            // 后台读取错误流，不可返回到前端
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("{} 输出: {}", processName, errorLine);
            }
            exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new DockerException(processName + "失败，退出码: " + exitCode);
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

    private void executePythonScript(String datasetPath, Path targetDir) throws InterruptedException, IOException {
        ProcessBuilder pythonProcess = new ProcessBuilder(
                "python",
                "code/predict.py",
                "--testdataset_path", datasetPath); // 传递测试集路径给Python脚本
        pythonProcess.directory(new File(targetDir.toString()));

        executeProcessWithLogging(pythonProcess, "Python运行");
    }

    @Override
    public EvaluationResultDO processClassificationEvaluationResult(String predictCsvPath, String groundTruthCsvPath, Long submissionId) throws IOException{
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

    // 一般二分类才使用recall，precision，f1-score
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
//                if (submitType.equals("MODEL")) {
                this.evaluateModel(submissionId, submitType);
//                }
//                else if (submitType.equals("DOCKER")) {
//                    this.evaluateDocker(submissionId);
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
