package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.dto.UserRankDTO;
import com.ecode.modelevalplat.service.RankListService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class RankListServiceImpl implements RankListService {
    @Override
    public void refreshCompetitionRankList(Long competitionId) {

    }

    @Override
    public Page<UserRankDTO> getCompetitionRankList(Long competitionId, String sortBy, Pageable pageable) {
        return null;
    }

    @Override
    public UserRankDTO getUserRank(Long userId, Long competitionId) {
        return null;
    }
}
