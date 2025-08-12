package com.ecode.modelevalplat.service;


import com.ecode.modelevalplat.common.ResVo;
import org.springframework.transaction.annotation.Transactional;

public interface CompetitionRegistrationService {


    ResVo<Integer> registerCompetition(Long userId, Long competitionId);
}
