package com.ecode.modelevalplat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecode.modelevalplat.dao.mapper.CompetitionMapper;
import com.ecode.modelevalplat.dao.mapper.SubmissionMapper;
import com.ecode.modelevalplat.dto.HistoryDTO;
import com.ecode.modelevalplat.dao.mapper.HistoryMapper;
import com.ecode.modelevalplat.service.HistoryService;
import org.springframework.stereotype.Service;
import com.ecode.modelevalplat.dao.mapper.EvaluationResultMapper;

import javax.annotation.Resource;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, HistoryDTO> implements HistoryService {

    @Resource
    private HistoryMapper historyMapper;
    private EvaluationResultMapper evaluationMapper;
    private SubmissionMapper submissionMapper;

    @Override
    public Page<HistoryDTO> getHistoryRecords(Long userId, Integer pageNum, Integer pageSize) {
        return historyMapper.selectHistoryRecords(userId, pageNum, pageSize);
    }

    @Override
    public Page<HistoryDTO> getHistoryByCompetition(Long userId, Long competitionId, Integer pageNum, Integer pageSize) {
        return historyMapper.selectHistoryByCompetition(userId, competitionId, pageNum, pageSize);
    }
}
