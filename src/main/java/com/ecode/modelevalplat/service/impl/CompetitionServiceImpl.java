package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.service.CompetitionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompetitionServiceImpl implements CompetitionService {
    @Autowired
    private CompetitionMapper competitionMapper;


    //管理员/用户 查询比赛列表
    @Override
    public PageInfo<CompetitionDO> selectAllCompetition(int pageNum,int pageSize) {
        // 使用PageHelper启动分页，并指定排序
        PageHelper.startPage(pageNum, pageSize);
        List<CompetitionDO> list = competitionMapper.selectAllCompetition();
        return new PageInfo<>(list);
    }

    //管理员/用户 查询比赛数据集路径
    @Override
    public String selectPath(Long id) {
        return competitionMapper.selectPath(id);
    }

    //管理员 新增比赛(发布比赛)
    @Override
    public int publishCompetition(CompetitionDO competition) {
        return competitionMapper.insertCompetition(competition);
    }

    @Override
    public int deleteCompetition(Long id) {
        return competitionMapper.deleteCompetition(id);
    }

    @Override
    public int updateCompetitionDescription(Long competitionId, String description) {
        return competitionMapper.updateCompetitionDescription(competitionId,description);
    }

    @Override
    public int updateCompetitionStartTime(Long competitionId, LocalDateTime startTime) {
        return competitionMapper.updateCompetitionStartTime(competitionId,startTime);
    }

    @Override
    public int updateCompetitionEndTime(Long competitionId, LocalDateTime endTime) {
        return competitionMapper.updateCompetitionEndTime(competitionId,endTime);
    }


}
