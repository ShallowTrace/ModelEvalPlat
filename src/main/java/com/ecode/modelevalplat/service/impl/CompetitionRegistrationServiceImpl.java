package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.UserCompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.UserMapper;
import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.entity.UserCompetitionDO;
import com.ecode.modelevalplat.dao.entity.UserDO;
import com.ecode.modelevalplat.exception.BusinessException;
import com.ecode.modelevalplat.service.CompetitionRegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CompetitionRegistrationServiceImpl implements CompetitionRegistrationService {

    private final UserMapper userMapper;
    private final CompetitionMapper competitionMapper;
    private final UserCompetitionMapper userCompetitionMapper;

    public CompetitionRegistrationServiceImpl(UserMapper userMapper,
                                              CompetitionMapper competitionMapper,
                                              UserCompetitionMapper userCompetitionMapper) {
        this.userMapper = userMapper;
        this.competitionMapper = competitionMapper;
        this.userCompetitionMapper = userCompetitionMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int registerCompetition(Long userId, Long competitionId) {
        // 1. 验证用户存在性
        UserDO user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证比赛有效性
        CompetitionDO competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new BusinessException("比赛不存在");
        }

//         3. 检查比赛状态
        Date now = new Date();
        if (!competition.getIsActive()) {
            throw new BusinessException("比赛未激活");
        }
        if (now.before(competition.getStartTime())) {
            throw new BusinessException("比赛尚未开始");
        }
        if (now.after(competition.getEndTime())) {
            throw new BusinessException("比赛已结束");
        }

        // 4. 检查是否重复报名
        UserCompetitionDO existingRegistration = userCompetitionMapper.findByUserAndCompetition(userId, competitionId);
        if (existingRegistration != null) {
            throw new BusinessException("用户已报名该比赛");
        }

        // 5. 创建报名记录
        UserCompetitionDO newRegistration = new UserCompetitionDO();
        newRegistration.setUserId(userId);
        newRegistration.setCompetitionId(competitionId);
        newRegistration.setJoinedAt(new Date());
        userCompetitionMapper.insert(newRegistration);

        // 6. 更新比赛人数
        return competitionMapper.incrementParticipantCount(competitionId);
    }
}