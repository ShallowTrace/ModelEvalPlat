package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompetitionService {
    @Autowired
    private CompetitionMapper competitionMapper;

    //管理员/用户 查询比赛列表
    public List<CompetitionDO> selectAllCompetition() {
        return competitionMapper.selectAllCompetition();
    }

    //管理员 新增比赛(发布比赛)
    public int publishCompetition(CompetitionDO competition) {
        return competitionMapper.insertCompetition(competition);
    }

    public int deleteCompetition(Long id) {
        return competitionMapper.deleteCompetition(id);
    }

    public int updateCompetitionDescription(Long competitionId, String description) {
        return competitionMapper.updateCompetitionDescription(competitionId,description);
    }

    public int updateCompetitionStartTime(Long competitionId, LocalDateTime startTime) {
        return competitionMapper.updateCompetitionStartTime(competitionId,startTime);
    }

    public int updateCompetitionEndTime(Long competitionId, LocalDateTime endTime) {
        return competitionMapper.updateCompetitionEndTime(competitionId,endTime);
    }

    //用户 比赛报名 /api/competitions/{competitionId}/registrations
}
