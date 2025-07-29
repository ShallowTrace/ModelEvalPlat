package com.ecode.modelevalplat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import com.ecode.modelevalplat.dto.EvaluationResultDTO;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public interface EvalService extends IService<EvaluationResultDO> {
    /**
     * 根据提交ID获取模型存储路径
     *
     * @param submissionId 提交记录ID
     * @return 模型文件路径
     */
    public String getModelPath(Long submissionId);

    /**
     * 运行Python模型测试
     * 
     * @param submissionId 提交记录ID
     * @return 评估结果JSON字符串
     *
     */
    public void evaluateModel(Long submissionId);

    /**
     * 处理评估结果并构建评估结果对象，但暂不存入数据库
     * 
     * @param predictCsvPath     预测结果CSV文件路径
     * @param groundTruthCsvPath 真实结果CSV文件路径
     * @param submissionId       提交记录ID
     */
    public EvaluationResultDO processEvaluationResult(String predictCsvPath, String groundTruthCsvPath, Long submissionId) throws IOException;

    /**
     * 异步运行evaluateModel方法
     *
     * @param submissionId 提交记录ID
     */
    void asyncEvaluateModel(Long submissionId, String submitType);
}