package com.ecode.modelevalplat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import com.ecode.modelevalplat.dto.EvaluationResultDTO;

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
     * @param competitionId 竞赛ID
     * @return 评估结果JSON字符串
     *
     */
    public String evaluateModel(Long submissionId, Long competitionId);

    /**
     * 处理评估结果并保存
     * 
     * @param csvPath       评估结果CSV文件路径
     * @param evaluationResultId 评估结果ID
     */
    public void processEvaluationResult(String csvPath, Long evaluationResultId);

}