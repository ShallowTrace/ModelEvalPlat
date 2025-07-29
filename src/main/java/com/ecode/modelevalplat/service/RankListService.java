package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.dto.UserRankDTO;
import com.github.pagehelper.PageInfo;

public interface RankListService {
    // 获取比赛排行榜（分页）
    PageInfo<UserRankDTO> getCompetitionRankList(Long competitionId, int pageNum, int pageSize);

    // 获取用户当前排名
    UserRankDTO getUserRank(Long userId, Long competitionId);
}
