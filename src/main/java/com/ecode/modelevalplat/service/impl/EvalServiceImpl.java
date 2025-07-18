package com.ecode.modelevalplat.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.dto.EvaluationResultDTO;
import com.ecode.modelevalplat.service.EvalService;
import com.ecode.modelevalplat.dao.mapper.EvaluationResultMapper;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvalServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResultDO> implements EvalService {


    private BaseMapper<SubmissionDO> submissionMapper;
    private BaseMapper<EvaluationResultDO> evaluationResultMapper;

    @Override
    public String getModelPath(Long submissionId) {

        // LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
        // .eq(UserDO::getUsername, username);
        // UserDO userDO = baseMapper.selectOne(queryWrapper);
        LambdaQueryWrapper<SubmissionDO> queryWrapper = Wrappers.lambdaQuery(SubmissionDO.class)
                .eq(SubmissionDO::getId, submissionId);
//        SubmissionDO submission = baseMapper.selectOne(queryWrapper);
        SubmissionDO submission = submissionMapper.selectOne(queryWrapper);
        if (submission == null) {
            throw new IllegalArgumentException("Submission not found");
        }
        return submission.getModelPath();

    }

    @Override
    public String evaluateModel(Long submissionId, Long competitionId) {
        // 1.获取脚本地址
        String modelPath = getModelPath(submissionId);
        if (modelPath == null || modelPath.isEmpty()) {
            throw new RuntimeException("模型路径不存在 for submission: " + submissionId);
        }
        // 2. 创建CSV输出目录（确保目录存在）
        String csvOutputDir = "src/main/resources/output/csv/";
        Path outputPath = Paths.get(csvOutputDir);
        try {
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建CSV输出目录失败: " + e.getMessage(), e);
        }
        String csvFileName = "evaluation_result_" + submissionId + ".csv";
        String csvFilePath = csvOutputDir + csvFileName;

        // 3. 运行Python脚本并指定CSV输出路径
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                modelPath,
                "--output", csvFilePath, // 传递输出路径给Python脚本
                "--competitionId", competitionId.toString());

        int exitCode = -1;

         try {
        Process process = processBuilder.start();
        // 读取脚本输出（可选：记录日志）
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Python脚本输出: {}", line);

            }
        }
        // 读取错误流（可选：捕获脚本错误）
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
//                log.error("Python脚本错误: {}", errorLine);
                log.error("Python脚本错误: {}", errorLine);
            }
        }
        exitCode = process.waitFor();
    } catch (IOException | InterruptedException e) {
        Thread.currentThread().interrupt(); // 恢复中断状态
        throw new RuntimeException("Python脚本执行失败: " + e.getMessage(), e);
    }
         //评估表存入数据
        LambdaQueryWrapper<SubmissionDO> queryWrapper = Wrappers.lambdaQuery(SubmissionDO.class)
                .eq(SubmissionDO::getId, submissionId);
        SubmissionDO submission = submissionMapper.selectOne(queryWrapper);
        EvaluationResultDO evaluationResult = new EvaluationResultDO();
        evaluationResult.setUserId(submission.getUserId());
        evaluationResult.setCompetitionId(competitionId);
        evaluationResult.setSubmitTime(new Date());

        evaluationResultMapper.insert(evaluationResult);
        submission.setStatus(exitCode == 0 ? StatusEnum.SUCCESS.name() : StatusEnum.FAILED.name());
        submissionMapper.updateById(submission);

        return csvFilePath;
    }

    private void installRequirements(String envName, String requirementFilePath) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "conda", "run", "-n", envName, "pip", "install", "-r", requirementFilePath);
        try {
            Process process = processBuilder.start();
            // 读取脚本输出（可选：记录日志）
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("依赖安装输出: {}", line);
                }
            }
            // 读取错误流（可选：捕获脚本错误）
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("依赖安装错误: {}", errorLine);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("在 Conda 虚拟环境中安装依赖失败");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("安装依赖时出错: " + e.getMessage(), e);
        }
    }


    @Override

    public void processEvaluationResult(String csvPath, Long evaluationResultId) {
        // 系统提供的 CSV 文件路径，需根据实际情况修改
        String systemCsvPath = "path/to/system.csv";

        try {
            // 读取 CSV 文件
            List<String> predictedLabels = readLabelsFromCsv(csvPath);
            List<String> trueLabels = readLabelsFromCsv(systemCsvPath);

            // 计算评估指标
            double accuracy = calculateAccuracy(predictedLabels, trueLabels);
            double f1Score = calculateF1Score(predictedLabels, trueLabels);

            // 更新数据库
            EvaluationResultDO evaluationResult = evaluationResultMapper.selectById(evaluationResultId);
            if (evaluationResult != null) {
                evaluationResult.setScore((float) f1Score);
                // 可以扩展将准确率等其他指标存入 resultJson
                evaluationResult.setResultJson(String.format("{\"accuracy\": %.2f, \"f1Score\": %.2f}", accuracy, f1Score));
                evaluationResultMapper.updateById(evaluationResult);
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("处理评估结果失败: " + e.getMessage(), e);
        }
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

    private double calculateF1Score(List<String> predicted, List<String> actual) {
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

        return 2 * (precision * recall) / (precision + recall);
    }
}
