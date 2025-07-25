package com.ecode.modelevalplat.service.impl;


import com.ecode.modelevalplat.dao.mapper.EvaluationResultMapper;
import com.ecode.modelevalplat.dto.UserRankDTO;
import com.ecode.modelevalplat.service.RankListService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankListServiceImpl implements RankListService {

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PageInfo<UserRankDTO> getCompetitionRankList(Long competitionId, int pageNum, int pageSize) {

        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);
        List<UserRankDTO> rankList = evaluationResultMapper.selectCompetitionRank(competitionId);

        // 计算排名，并处理并列
        int currentRank = 1;
        Double previousScore = null;
        for (int i = 0; i < rankList.size(); i++) {
            UserRankDTO dto = rankList.get(i);
            double currentScore = dto.getPrimaryScore();
            if (previousScore == null || currentScore < previousScore) {
                currentRank = i + 1;
            }
            dto.setRank(currentRank);
            previousScore = currentScore;
        }

        return new PageInfo<>(rankList);
    }


    @Override
    public UserRankDTO getUserRank(Long userId, Long competitionId) {
        // 获取完整排行榜（不分页）
        PageInfo<UserRankDTO> fullRank = this.getCompetitionRankList(competitionId, 1, Integer.MAX_VALUE);

        // 查找指定用户的排名
        return fullRank.getList().stream()
                .filter(dto -> dto.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该用户无有效记录"));
    }
}