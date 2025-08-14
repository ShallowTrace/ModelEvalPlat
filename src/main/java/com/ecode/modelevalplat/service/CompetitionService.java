package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;

public interface CompetitionService {

    //管理员/用户 查询比赛列表
    public PageInfo<CompetitionDO> selectAllCompetition(int pageNum,int pageSize);

    //管理员/用户 查询比赛数据集路径
    String selectPath(Long id);

    //管理员/用户 查询单个比赛
    public CompetitionDO selectCompetitionById(Long id);

    //管理员 新增比赛(发布比赛)
    public int publishCompetition(CompetitionDO competition);

    public int deleteCompetition(Long id) ;

    public int updateCompetitionDescription(Long competitionId, String description);

    public int updateCompetitionStartTime(Long competitionId, LocalDateTime startTime) ;

    public int updateCompetitionEndTime(Long competitionId, LocalDateTime endTime);
    //用户 比赛报名 /api/competitions/{competitionId}/registrations
}
