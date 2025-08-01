package com.ecode.modelevalplat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import com.ecode.modelevalplat.dto.HistoryDTO;
import com.ecode.modelevalplat.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * 查询用户所有历史记录
     */
    @GetMapping("/user/{userId}")
    public ResVo<Page<EvaluationResultDO>> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EvaluationResultDO> result = historyService.getHistoryRecords(userId, page, size);
        return ResVo.ok(result);
    }

    /**
     * 查询用户特定比赛历史记录
     */
    @GetMapping("/competition/{competitionId}/user/{userId}")
    public ResVo<Page<EvaluationResultDO>> getCompetitionHistory(
            @PathVariable Long userId,
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EvaluationResultDO> result = historyService.getHistoryByCompetition(
                userId, competitionId, page, size);
        return ResVo.ok(result);


    }
}