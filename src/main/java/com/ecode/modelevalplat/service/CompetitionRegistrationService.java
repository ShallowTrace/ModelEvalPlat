package com.ecode.modelevalplat.service;


import org.springframework.transaction.annotation.Transactional;

public interface CompetitionRegistrationService {

    @Transactional(rollbackFor = Exception.class)
    int registerCompetition(Long userId, Long competitionId);
}
