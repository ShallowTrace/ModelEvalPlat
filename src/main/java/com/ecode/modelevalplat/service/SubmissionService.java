package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.dto.SubmissionResp;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;


public interface SubmissionService {
    // 处理模型提交
    ResVo<SubmissionResp> submitModel(Long userId, Long competitionId, String submitType, MultipartFile file);

    // 获取用户提交历史（分页）
    PageInfo<SubmissionDO> getUserSubmissions(Long userId, Long competitionId, int pageNum, int pageSize);

    // 检查当日剩余提交次数
    int checkDailyQuota(Long userId, Long competitionId);
}