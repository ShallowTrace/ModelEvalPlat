package com.ecode.modelevalplat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ecode.modelevalplat.dto.HistoryDTO;
import com.ecode.modelevalplat.dao.mapper.HistoryMapper;
import com.ecode.modelevalplat.service.HistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, HistoryDTO> implements HistoryService {

    @Resource
    private HistoryMapper historyMapper;

    @Override
    public Page<HistoryDTO> getHistoryRecords(Long userId, Integer pageNum, Integer pageSize) {
        Page<HistoryDTO> page = new Page<>(pageNum, pageSize);
        return historyMapper.selectHistoryRecords(page, userId);
    }

    @Override
    public Page<HistoryDTO> getHistoryByCompetition(Long userId, Long competitionId, Integer pageNum,
            Integer pageSize) {
        Page<HistoryDTO> page = new Page<>(pageNum, pageSize);
        return historyMapper.selectHistoryByCompetition(page, userId, competitionId);
    }
}
