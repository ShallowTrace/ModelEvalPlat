package com.ecode.modelevalplat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecode.modelevalplat.dto.HistoryDTO;



public interface HistoryService extends IService<HistoryDTO> {

        /**
         * 分页查询用户历史记录
         * @param userId 用户ID
         * @param pageNum 页码
         * @param pageSize 每页数量
         * @return 分页历史记录列表
         */
        Page<HistoryDTO> getHistoryRecords(Long userId, Integer pageNum, Integer pageSize);

        /**
         * 按比赛分页查询历史记录
         * @param userId 用户ID
         * @param competitionId 比赛ID
         * @param pageNum 页码
         * @param pageSize 每页数量
         * @return 分页历史记录列表
         */
        Page<HistoryDTO> getHistoryByCompetition(Long userId, Long competitionId, Integer pageNum, Integer pageSize);
    }

