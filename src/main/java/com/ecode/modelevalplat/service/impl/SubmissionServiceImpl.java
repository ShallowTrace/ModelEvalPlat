package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.EvalStatusEnum;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import com.ecode.modelevalplat.dao.mapper.UserCompetitionMapper;
import com.ecode.modelevalplat.dto.SubmissionResp;
import com.ecode.modelevalplat.service.SubmissionService;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    @Transactional(rollbackFor = Exception.class)
    // TODO 用户鉴权
    public ResVo<SubmissionResp> submitModel(Long userId, Long competitionId, String submitType, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        try {
            // 1. 空文件检查（只检查文件大小，不对实质内容进行检查）
            if (file.isEmpty()) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "文件内容为空");
//                return SubmissionResp.failure("文件内容为空", "EMPTY_FILE", originalFilename);
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
                return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "用户未报名该比赛");
//                return SubmissionResp.failure("用户未报名该比赛", "NO_PERMISSION", originalFilename);
            }

            // 4. 比赛状态校验
            // TODO 改为使用数据库的is_active字段来校验
            CompetitionDO competition = competitionMapper.findById(competitionId);
            Instant now = Instant.now();
            Instant start = competition.getStartTime().toInstant();
            Instant end = competition.getEndTime().toInstant();
            if (now.isBefore(start) || now.isAfter(end)) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "比赛未在进行中");
//                return SubmissionResp.failure("比赛未在进行中", "COMPETITION_NOT_ACTIVE", originalFilename);
            }

            // 5. 每日配额校验
            if (checkDailyQuota(userId, competitionId) >= competition.getDailySubmissionLimit()) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "今日提交次数已达上限");
//                return SubmissionResp.failure("今日提交次数已达上限", "QUOTA_EXCEEDED", originalFilename);
            }

            // 6. 文件扩展名校验、检查文件魔数（真实文件类型）
            if (!originalFilename.matches(".*\\.zip$")) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "仅支持ZIP格式");
//                return SubmissionResp.failure("仅支持ZIP格式", "INVALID_FILE_TYPE", originalFilename);
            }

            InputStream is = file.getInputStream();
            byte[] fileHead = new byte[4];
            is.read(fileHead);
            // 验证是否是ZIP文件（PK头）
            if (!Arrays.equals(fileHead, new byte[]{0x50, 0x4B, 0x03, 0x04})) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "非法的文件格式");
//                return SubmissionResp.failure("非法的文件格式", "INVALID_FILE_TYPE", originalFilename);
            }

            // 7. 按提交类型校验
            ResVo<SubmissionResp> validation = submitType.equals("MODEL")
                    ? handleModelFile(file)
                    : handleDockerFile(file);
            if (validation.getStatus().getCode() != 0) {
                return validation;
            }

            // 8. 持久化存储
            String persistPath = String.format("%s/%d/%d/%s",
                    MODELFILE_STORAGE_PATH, competitionId, userId, fileName);
            File destFile = new File(persistPath);
            destFile.getParentFile().mkdirs();

            // 9. 在mysql数据库中创建提交记录
            SubmissionDO submission = new SubmissionDO();
            submission.setUserId(userId);
            submission.setCompetitionId(competitionId);
            submission.setModelPath(persistPath);
            submission.setStatus(EvalStatusEnum.PENDING.name());
            submission.setSubmitTime(new Date());
            submissionMapper.insert(submission);

            // 10. 最后保存文件（如果失败会触发事务回滚）
            file.transferTo(destFile.toPath());

            return ResVo.ok(SubmissionResp.success(submission.getId(), originalFilename));
//            return SubmissionResp.success(submission.getId(), originalFilename);

        } catch (IOException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResVo.fail(StatusEnum.SAVE_FILE_FAILED, e.getMessage());
//            return SubmissionResp.failure("文件存储失败: " + e.getMessage(), "FILE_STORAGE_ERROR", originalFilename);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, e.getMessage());
//            return SubmissionResp.failure("系统错误: " + e.getMessage(), "SYSTEM_ERROR", originalFilename);
        }
    }

    @Override
    public PageInfo<SubmissionDO> getUserSubmissions(Long userId, Long competitionId, int pageNum, int pageSize) {
        // 使用PageHelper启动分页，并指定排序
        PageHelper.startPage(pageNum, pageSize);
        List<SubmissionDO> list = submissionMapper.findByUserAndCompetition(userId, competitionId);
        return new PageInfo<>(list);
    }


    @Override
    public int checkDailyQuota(Long userId, Long competitionId) {
        // 查询当日提交次数
        return submissionMapper.countTodaySubmissions(
                userId,
                competitionId
        );
    }



    public ResVo<SubmissionResp> handleModelFile(MultipartFile file) {
        try (ZipInputStream zipIn = new ZipInputStream(file.getInputStream())) {
            int predictPyCount = 0;
            int environmentJsonCount = 0;
            int requirementsTxtCount = 0;
            boolean hasPredictionResultDir = false;

            // 遍历ZIP文件条目
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 处理目录条目（检查prediction_result目录）
                if (entry.isDirectory()) {
                    if (entryName.equals("prediction_result/")) {
                        hasPredictionResultDir = true;
                    }
                    continue;
                }

                // 处理文件路径
                if (entryName.startsWith("code/")) {
                    // 检查code目录下的predict.py
                    if (entryName.equals("code/predict.py")) {
                        predictPyCount++;
                    }
                } else {
                    // 检查根目录的requirements.txt
                    if (entryName.equals("requirements.txt")) {
                        requirementsTxtCount++;
                    }
                    if(entryName.equals("environment.json")){
                        // 检查根目录的environment.json
                        environmentJsonCount++;
                    }
                }
            }

            // 验证文件结构
            if (predictPyCount == 0) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP包code目录缺少predict.py文件");
            }
            if (environmentJsonCount == 0) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP包根目录缺少environment.json文件");
            }
            if (requirementsTxtCount == 0) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP包根目录缺少requirements.txt文件");
            }
            if (!hasPredictionResultDir) {
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP包根目录缺少prediction_result目录");
            }

            return ResVo.ok(SubmissionResp.success(null, file.getOriginalFilename()));

        } catch (IOException e) {
            return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP文件读取失败: " + e.getMessage());
        }
    }


    public ResVo<SubmissionResp> handleDockerFile(MultipartFile file) {
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
                return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP包中缺少Dockerfile文件");
//                return SubmissionResp.failure("ZIP包中缺少Dockerfile文件", "MISSING_DOCKERFILE", file.getOriginalFilename());
            }

            return ResVo.ok(SubmissionResp.success(null, file.getOriginalFilename()));
//            return SubmissionResp.success(null, file.getOriginalFilename());

        } catch (IOException e) {
            return ResVo.fail(StatusEnum.SUBMISSION_FAILED_MIXED, "ZIP文件读取失败: " + e.getMessage());
//            return SubmissionResp.failure("ZIP文件读取失败: " + e.getMessage(), "INVALID_ZIP", file.getOriginalFilename());
        }
    }
}
