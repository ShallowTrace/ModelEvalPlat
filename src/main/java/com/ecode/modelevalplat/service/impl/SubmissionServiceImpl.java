package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.service.SubmissionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    // 提交的文件使用固定存储路径（可以根据需要修改）
    private static final String FILE_STORAGE_PATH = "C:\\Users\\ShallowTrace\\Desktop\\submission\\";


    @Override
    public SubmissionDO submitModel(Long userId, Long competitionId, MultipartFile file) {
        return null;
    }

    @Override
    public Page<SubmissionDO> getUserSubmissions(Long userId, Long competitionId, Pageable pageable) {
        return null;
    }

    @Override
    public String saveUploadedFile(MultipartFile file) throws IOException {
        File storageDir = new File(FILE_STORAGE_PATH);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // 构建目标文件路径
        File dest = new File(FILE_STORAGE_PATH + file.getOriginalFilename());

        // 保存文件到指定路径
        file.transferTo(dest);

        return "文件上传成功：" + file.getOriginalFilename();
    }

    @Override
    public int checkDailyQuota(Long userId, Long competitionId) {
        return 0;
    }

    @Override
    public boolean handleModelFile(Long competitionId) {
        return false;
    }

    @Override
    public boolean handleDockerFile(Long competitionId) {
        return false;
    }
}
