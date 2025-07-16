package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dto.UserRankDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RankListService {
    // 刷新比赛排行榜（由提交回调触发）
    void refreshCompetitionRankList(Long competitionId);

    // 获取比赛排行榜（分页）
    Page<UserRankDTO> getCompetitionRankList(Long competitionId, String sortBy, Pageable pageable);

    // 获取用户当前排名
    UserRankDTO getUserRank(Long userId, Long competitionId);
}
