package com.ecode.modelevalplat.controller;



import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.SubmissionResp;
import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import com.ecode.modelevalplat.service.SubmissionService;


import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/competitions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @ResponseBody
    @PostMapping("/{competitionId}/submissions")
    public ResVo<SubmissionResp> handleFileUpload(
            @PathVariable Long competitionId,
            @RequestParam("submitType") String submitType, // 提交类型，包括 "MODEL","DOCKER"两种
            @RequestParam(value = "modelPackage", required = false) MultipartFile modelFile,
            @RequestParam(value = "dockerFile", required = false) MultipartFile dockerFile) {

//        MultipartFile targetFile = submitType.equals("MODEL") ? modelFile : dockerFile;
        // TODO 鉴权，这里的userId是写死的，后期要改成类似从token中获取的方案
        if (submitType.equals("MODEL")) {
            return submissionService.submitModel(1001L, competitionId, submitType, modelFile);
        }
        else {
            return submissionService.submitModel(1001L, competitionId, submitType, dockerFile);
        }
    }

    // 提交记录查询，按时间倒序
    @GetMapping("/{competitionId}/submissionhistory")
    public ResVo<PageInfo<SubmissionDO>> getSubmissions(
            // TODO 鉴权，这里的userId是写死的，后期要改成类似从token中获取的方案
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<SubmissionDO> submissions = submissionService.getUserSubmissions(1001L, competitionId, pageNum, pageSize);
        return ResVo.ok(submissions);
    }

}
