package com.ecode.modelevalplat.service;


import com.ecode.modelevalplat.common.ResVo;
import org.springframework.web.multipart.MultipartFile;

public interface CompetitionRegistrationService {
    ResVo<Integer> registerCompetition(Long userId, Long competitionId);
    ResVo<Integer> cancelRegistration(Long userId, Long competitionId);
    ResVo<Integer> uploadCompetitionDataset(Long competitionId, MultipartFile file);

}
