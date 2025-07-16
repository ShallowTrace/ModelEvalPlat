package com.ecode.modelevalplat.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import com.ecode.modelevalplat.dto.EvaluationResultDTO;
import com.ecode.modelevalplat.service.EvalService;
import com.ecode.modelevalplat.dao.mapper.EvaluationResultMapper;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;

@Service
public class EvalServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResultDO> implements EvalService {
    @Autowired
    private SubmissionMapper submissionMapper;

    @Override
    public String getModelPath(Long submissionId) {

        // LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
        // .eq(UserDO::getUsername, username);
        // UserDO userDO = baseMapper.selectOne(queryWrapper);
        LambdaQueryWrapper<SubmissionDO> queryWrapper = Wrappers.lambdaQuery(SubmissionDO.class)
                .eq(SubmissionDO::getId, submissionId);
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
                log.info("Python脚本输出: {}", line); // 需要添加日志依赖
            }
        }
        // 读取错误流（可选：捕获脚本错误）
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("Python脚本错误: {}", errorLine);
            }
        }
        exitCode = process.waitFor();
    } catch (IOException | InterruptedException e) {
        Thread.currentThread().interrupt(); // 恢复中断状态
        throw new RuntimeException("Python脚本执行失败: " + e.getMessage(), e);
    }
        EvaluationResultDO evaluationResult = new EvaluationResultDO();
        evaluationResult.setUserId(submission.getUserId());
        evaluationResult.setCompetitionId(competitionId);
        evaluationResult.setResultJson(resultJson.toString());
        evaluationResultMapper.insert(evaluationResult);

        submission.setStatus(exitCode == 0 ? StatusEnum.SUCCESS.name() : StatusEnum.FAILED.name());
        submissionMapper.updateById(submission);

        throw new UnsupportedOperationException("Unimplemented method 'evaluateModel'");
    }

    @Override
    public void processEvaluationResult(EvaluationResultDTO result, Long submissionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processEvaluationResult'");
    }

}
