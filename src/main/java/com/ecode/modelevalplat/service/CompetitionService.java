package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

public interface CompetitionService {

    //管理员/用户 查询比赛列表
    public List<CompetitionDO> selectAllCompetition();

    //管理员/用户 查询比赛数据集路径
    String selectPath(Long id);

    //管理员 新增比赛(发布比赛)
    public int publishCompetition(CompetitionDO competition);

    public int deleteCompetition(Long id) ;

    public int updateCompetitionDescription(Long competitionId, String description);

    public int updateCompetitionStartTime(Long competitionId, LocalDateTime startTime) ;

    public int updateCompetitionEndTime(Long competitionId, LocalDateTime endTime);
    //用户 比赛报名 /api/competitions/{competitionId}/registrations
}
