package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import com.ecode.modelevalplat.dao.mapper.UserCompetitionMapper;
import com.ecode.modelevalplat.dto.SubmissionResp;
import com.ecode.modelevalplat.service.SubmissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    // 提交的文件使用固定存储路径（可以根据需要修改）
    @Value("${MODELFILE_STORAGE_PATH}")
    private String MODELFILE_STORAGE_PATH;

    @Autowired
    private UserCompetitionMapper userCompetitionMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private SubmissionMapper submissionMapper;


    @Override
    // TODO 用户鉴权
    public SubmissionResp submitModel(Long userId, Long competitionId, String submitType, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        try {
            // 1. 空文件检查
            if (file.isEmpty()) {
                return SubmissionResp.failure("文件内容为空", "EMPTY_FILE", originalFilename);
            }

            // 2. 生成唯一文件名（保持原扩展名）
            String ext = originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = String.format("%s_%d_%d_%d%s",
                    UUID.randomUUID().toString().replace("-", ""),
                    competitionId,
                    userId,
                    System.currentTimeMillis(),
                    ext);

            // 3. 用户是否报名校验
            if (userCompetitionMapper.findByUserAndCompetition(userId, competitionId) == null) {
                return SubmissionResp.failure("用户未报名该比赛", "NO_PERMISSION", originalFilename);
            }

            // 4. 比赛状态校验
            CompetitionDO competition = competitionMapper.findById(competitionId);
            Instant now = Instant.now();
            Instant start = competition.getStartTime().toInstant();
            Instant end = competition.getEndTime().toInstant();
            if (now.isBefore(start) || now.isAfter(end)) {
                return SubmissionResp.failure("比赛未在进行中", "COMPETITION_NOT_ACTIVE", originalFilename);
            }

            // 5. 每日配额校验
            if (checkDailyQuota(userId, competitionId) >= competition.getDailySubmissionLimit()) {
                return SubmissionResp.failure("今日提交次数已达上限", "QUOTA_EXCEEDED", originalFilename);
            }

            // 6. 文件扩展名校验
            if (!originalFilename.matches(".*\\.zip$")) {
                return SubmissionResp.failure("仅支持ZIP格式", "INVALID_FILE_TYPE", originalFilename);
            }

            // 7. 按提交类型校验
            SubmissionResp validation = submitType.equals("MODEL")
                    ? handleModelFile(file)
                    : handleDockerFile(file);
            if (!validation.isSuccess()) {
                return validation;
            }

            // 8. 持久化存储
            String persistPath = String.format("%s/%d/%d/%s",
                    MODELFILE_STORAGE_PATH, competitionId, userId, fileName);
            File destFile = new File(persistPath);
            destFile.getParentFile().mkdirs();

            // 保存文件
            file.transferTo(destFile.toPath());

            // 9. 创建提交记录
            SubmissionDO submission = new SubmissionDO();
            submission.setUserId(userId);
            submission.setCompetitionId(competitionId);
            submission.setModelPath(persistPath);
            submission.setStatus(StatusEnum.PENDING.name());
            submission.setSubmitTime(new Date());
            submissionMapper.insert(submission);

            return SubmissionResp.success(submission.getId(), originalFilename);

        } catch (IOException e) {
            return SubmissionResp.failure("文件存储失败: " + e.getMessage(), "FILE_STORAGE_ERROR", originalFilename);
        } catch (Exception e) {
            return SubmissionResp.failure("系统错误: " + e.getMessage(), "SYSTEM_ERROR", originalFilename);
        }
    }

    @Override
    public Page<SubmissionDO> getUserSubmissions(Long userId, Long competitionId, Pageable pageable) {
        // 添加排序条件（按提交时间倒序）
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "submitTime")
        );
        return submissionMapper.findByUserAndCompetition(userId, competitionId, sortedPageable);
    }


    @Override
    public int checkDailyQuota(Long userId, Long competitionId) {
        // 查询当日提交次数
        return submissionMapper.countTodaySubmissions(
                userId,
                competitionId
        );
    }

    @Override
    public SubmissionResp handleModelFile(MultipartFile file) {
        try (ZipInputStream zipIn = new ZipInputStream(file.getInputStream())) {
            int predictPyCount = 0;
            int requirementsTxtCount = 0;

            // 遍历ZIP文件条目
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 跳过目录和嵌套文件（只检查根目录）
                if (entry.isDirectory() || entryName.contains("/")) {
                    continue;
                }

                // 统计目标文件
                if (entryName.equals("predict.py")) {
                    predictPyCount++;
                } else if (entryName.equals("requirements.txt")) {
                    requirementsTxtCount++;
                }
            }

            // 验证文件数量
            if (predictPyCount == 0) {
                return SubmissionResp.failure("ZIP包根目录缺少predict.py文件", "MISSING_PREDICT_PY", file.getOriginalFilename());
            }
            if (predictPyCount > 1) {
                return SubmissionResp.failure("ZIP包根目录包含多个predict.py文件", "MULTIPLE_PREDICT_PY", file.getOriginalFilename());
            }
            if (requirementsTxtCount == 0) {
                return SubmissionResp.failure("ZIP包根目录缺少requirements.txt文件", "MISSING_REQUIREMENTS", file.getOriginalFilename());
            }
            if (requirementsTxtCount > 1) {
                return SubmissionResp.failure("ZIP包根目录包含多个requirements.txt文件", "MULTIPLE_REQUIREMENTS", file.getOriginalFilename());
            }

            return SubmissionResp.success(null, file.getOriginalFilename());

        } catch (IOException e) {
            return SubmissionResp.failure("ZIP文件读取失败: " + e.getMessage(), "INVALID_ZIP", file.getOriginalFilename());
        }
    }

    @Override
    public SubmissionResp handleDockerFile(MultipartFile file) {
        try (ZipInputStream zipIn = new ZipInputStream(file.getInputStream())) {
            int dockerfileCount = 0;

            // 遍历ZIP文件条目
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 跳过目录和嵌套文件（只检查根目录）
                if (entry.isDirectory() || entryName.contains("/")) {
                    continue;
                }

                // 统计Dockerfile文件
                if (entryName.equals("Dockerfile")) {
                    dockerfileCount++;
                }
            }

            // 验证文件数量
            if (dockerfileCount == 0) {
                return SubmissionResp.failure("ZIP包中缺少Dockerfile文件", "MISSING_DOCKERFILE", file.getOriginalFilename());
            }
            if (dockerfileCount > 1) {
                return SubmissionResp.failure("ZIP包中包含多个Dockerfile文件", "MULTIPLE_DOCKERFILES", file.getOriginalFilename());
            }

            return SubmissionResp.success(null, file.getOriginalFilename());

        } catch (IOException e) {
            return SubmissionResp.failure("ZIP文件读取失败: " + e.getMessage(), "INVALID_ZIP", file.getOriginalFilename());
        }
    }
}
