package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SubmissionService {
    // 处理模型提交
    SubmissionDO submitModel(Long userId, Long competitionId, MultipartFile file);

    // 获取用户提交历史（分页）
    Page<SubmissionDO> getUserSubmissions(Long userId, Long competitionId, Pageable pageable);

    // 保存上传的文件
    String saveUploadedFile(MultipartFile file) throws IOException;

    // 检查当日剩余提交次数
    int checkDailyQuota(Long userId, Long competitionId);

    // 检查提交的模型文件是否合法
    boolean handleModelFile(Long competitionId);

    // 检查提交的docker文件是否合法
    boolean handleDockerFile(Long competitionId);
}