package com.ecode.modelevalplat.controller;



import com.ecode.modelevalplat.dto.SubmissionResp;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.service.SubmissionService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.web.PageableDefault;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/competitions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;
    

    @PostMapping("/{competitionId}/submissions")
    public SubmissionResp handleFileUpload(
            @PathVariable Long competitionId,
            @RequestParam("submitType") String submitType, // 提交类型，包括 "MODEL","DOCKER"两种
            @RequestParam("modelPackage") MultipartFile modelFile,
            @RequestParam("dockerFile") MultipartFile dockerFile) {

        MultipartFile targetFile = submitType.equals("MODEL") ? modelFile : dockerFile;
        SubmissionResp submissionResp=submissionService.submitModel(1L, competitionId, submitType, targetFile);
        return submissionResp;
    }

    // 提交记录查询，按时间倒序
    @GetMapping("/submissionhistory")
    public Page<SubmissionDO> getSubmissions(
            @RequestParam Long userId,
            @RequestParam Long competitionId,
            @PageableDefault(sort = "submitTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return submissionService.getUserSubmissions(userId, competitionId, pageable);
    }

}
