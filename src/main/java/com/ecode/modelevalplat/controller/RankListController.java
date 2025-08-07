package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.UserRankDTO;
import com.ecode.modelevalplat.service.RankListService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

@RestController

public class RankListController {

    private final RankListService rankListService;

    @Autowired
    public RankListController(RankListService rankListService) {
        this.rankListService = rankListService;
    }

    /**
     * 获取比赛排行榜（分页）
     */
    @GetMapping("/competitions/{competitionId}/rankingList")
    public ResVo<PageInfo<UserRankDTO>> getCompetitionRankingList(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<UserRankDTO> rankList = rankListService.getCompetitionRankList(competitionId, pageNum, pageSize);
        return ResVo.ok(rankList);
    }

    /**
     * 获取用户当前排名
     */
    @GetMapping("/user/{userId}/competitions/{competitionId}")
    public ResVo<UserRankDTO> getUserRank(
            @PathVariable Long userId,
            @PathVariable Long competitionId) {
        UserRankDTO userRank = rankListService.getUserRank(userId, competitionId);
        return ResVo.ok(userRank);
    }
}