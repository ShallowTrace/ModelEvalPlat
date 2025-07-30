package com.ecode.modelevalplat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import com.ecode.modelevalplat.dto.EvaluationResultDTO;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.nio.file.Path;

public interface EvalService extends IService<EvaluationResultDO> {

    /**
     *
     * 从查询提交记录开始运行模型评估全流程
     * 
     * @param submissionId 提交记录ID
     *
     */
    public void evaluateModel(Long submissionId);

    /**
     * 运行Docker模型测试
     *
     * @param datasetPath 测试数据集路径
     * @param targetDir   指定的工作目录
     */
    public void executeDocker(String datasetPath, Path targetDir) throws InterruptedException, IOException;

    /**
     * 处理评估结果并构建评估结果对象，但暂不存入数据库
     * 
     * @param predictCsvPath     预测结果CSV文件路径
     * @param groundTruthCsvPath 真实结果CSV文件路径
     * @param submissionId       提交记录ID
     * @return 如没有异常则代表评估成功，返回本次评估的评估结果实体类
     */
    public EvaluationResultDO processEvaluationResult(String predictCsvPath, String groundTruthCsvPath, Long submissionId) throws IOException;

    /**
     * 异步运行evaluateModel方法
     *
     * @param submissionId 提交记录ID
     */
    public void asyncEvaluateModel(Long submissionId, String submitType);
}
