package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.config.CosConfig;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.UserCompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.UserMapper;
import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.entity.UserCompetitionDO;
import com.ecode.modelevalplat.dao.entity.UserDO;
import com.ecode.modelevalplat.common.exception.CompetitionException;
import com.ecode.modelevalplat.service.CompetitionRegistrationService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompetitionRegistrationServiceImpl implements CompetitionRegistrationService {
//    @Autowired
//    private UserMapper userMapper;
//    @Autowired
//    private CompetitionMapper competitionMapper;
//    @Autowired
//    private UserCompetitionMapper userCompetitionMapper;
//    @Autowired
//    private COSClient cosClient;
//    @Value("${spring.cos.bucketName}")
//    private final String Bucket_Name;
    private final UserMapper userMapper;
    private final CompetitionMapper competitionMapper;
    private final UserCompetitionMapper userCompetitionMapper;
    private final COSClient cosClient;
    private final CosConfig.PutObjectRequestBuilder putRequestBuilder;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResVo<Integer> registerCompetition(Long userId, Long competitionId) {
        try {
            // 1. 验证用户存在性
            UserDO user = userMapper.findById(userId);
            if (user == null) {
                throw new CompetitionException("用户不存在");
            }

            // 2. 验证比赛有效性
            CompetitionDO competition = competitionMapper.findById(competitionId);
            if (competition == null) {
                throw new CompetitionException("比赛不存在");
            }

            //3. 检查比赛状态
            Date now = new Date();
            if (!competition.getIsActive()) {
                throw new CompetitionException("比赛未激活");
            }
            if (now.before(competition.getStartTime())) {
                throw new CompetitionException("比赛尚未开始");
            }
            if (now.after(competition.getEndTime())) {
                throw new CompetitionException("比赛已结束");
            }

            // 4. 检查是否重复报名
            UserCompetitionDO existingRegistration = userCompetitionMapper.findByUserAndCompetition(userId, competitionId);

            if (existingRegistration != null) {
                throw new CompetitionException("用户已报名该比赛");
            }

            // 5. 创建报名记录
            UserCompetitionDO newRegistration = new UserCompetitionDO();
            newRegistration.setUserId(userId);
            newRegistration.setCompetitionId(competitionId);
            newRegistration.setJoinedAt(new Date());
            userCompetitionMapper.insert(newRegistration);

//      6. 更新比赛人数
            competitionMapper.incrementParticipantCount(competitionId);
            return ResVo.ok(StatusEnum.COMPETITION_REGISTRATION_SUCCESS, 1);
        } catch (CompetitionException e) {
            if (e.getMessage().equals("用户不存在")) {
                return ResVo.fail(StatusEnum.USER_NOT_FOUND);
            } else if (e.getMessage().equals("比赛不存在")) {
                return ResVo.fail(StatusEnum.COMPETITION_NOT_FOUND);
            } else if (e.getMessage().equals("比赛未激活")) {
                return ResVo.fail(StatusEnum.COMPETITION_INACTIVE);
            } else if (e.getMessage().equals("比赛尚未开始")) {
                return ResVo.fail(StatusEnum.COMPETITION_NOT_STARTED);
            }else if (e.getMessage().equals("比赛已结束")) {
                return ResVo.fail(StatusEnum.COMPETITION_ENDED);
            }else if (e.getMessage().equals("用户已报名该比赛")) {
                return ResVo.fail(StatusEnum.ALREADY_REGISTERED);
            }
            else
                return ResVo.fail(StatusEnum.REGISTRATION_SYSTEM_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResVo<Integer> cancelRegistration(Long userId, Long competitionId) {
        try {
            // 1. 验证用户存在性
            UserDO user = userMapper.findById(userId);
            if (user == null) {
                throw new CompetitionException("用户不存在");
            }

            // 2. 验证比赛有效性
            CompetitionDO competition = competitionMapper.findById(competitionId);
            if (competition == null) {
                throw new CompetitionException("比赛不存在");
            }

            // 3. 检查比赛状态（是否允许取消）
            Date now = new Date();
            if (now.after(competition.getStartTime())) {
                throw new CompetitionException("比赛已开始，不可取消报名");
            }

            // 4. 检查报名记录是否存在
            UserCompetitionDO existingRegistration = userCompetitionMapper.findByUserAndCompetition(userId, competitionId);
            if (existingRegistration == null) {
                throw new CompetitionException("未找到报名记录");
            }

            // 5. 删除报名记录
            userCompetitionMapper.deleteById(existingRegistration.getId());

            // 6. 更新比赛人数（减少）
            competitionMapper.decrementParticipantCount(competitionId);

            return ResVo.ok(StatusEnum.COMPETITION_CANCEL_SUCCESS, 1);
        } catch (CompetitionException e) {
            // 异常类型映射
            if (e.getMessage().equals("用户不存在")) {
                return ResVo.fail(StatusEnum.USER_NOT_FOUND);
            } else if (e.getMessage().equals("比赛不存在")) {
                return ResVo.fail(StatusEnum.COMPETITION_NOT_FOUND);
            } else if (e.getMessage().equals("比赛已开始，不可取消报名")) {
                return ResVo.fail(StatusEnum.CANCEL_NOT_ALLOWED);
            } else if (e.getMessage().equals("未找到报名记录")) {
                return ResVo.fail(StatusEnum.REGISTRATION_NOT_FOUND);
            } else {
                return ResVo.fail(StatusEnum.CANCEL_SYSTEM_ERROR);
            }
        }
    }

    @Override
    public ResVo<Integer> uploadCompetitionDataset(Long competitionId, MultipartFile file) {
        try (InputStream fileStream = file.getInputStream()) {
            // 1. 生成唯一文件名
            String objectKey = generateObjectKey(competitionId, file);

            // 2. 创建元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 3. 使用构建器创建请求
            PutObjectRequest putRequest = putRequestBuilder.build(objectKey, fileStream, metadata);

            // 4. 执行上传
            cosClient.putObject(putRequest);
            return ResVo.ok(1);
        } catch (IOException  e) {
            log.error("文件上传失败", e);
            return ResVo.fail(StatusEnum.FILE_UPLOAD_FAIL);
        }
    }

    private String generateObjectKey(Long competitionId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "competition/" + competitionId + "/dataset_"
                + System.currentTimeMillis() + extension;
    }
}