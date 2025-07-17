package com.ecode.modelevalplat.controller;



import com.ecode.modelevalplat.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;


    @GetMapping("/upload")
    public String showUploadForm() {
        return "<html>" +
                "<body>" +
                "<h3>选择提交类型：</h3>" +
                "<form method=\"POST\" action=\"/api/submission/upload\" enctype=\"multipart/form-data\">" +
                "<input type=\"radio\" name=\"submitType\" value=\"MODEL\" checked> 模型提交（请打包为一个文件）<br>" +
                "<input type=\"radio\" name=\"submitType\" value=\"DOCKER\"> Docker镜像提交<br><br>" +

                "<div id=\"model-section\">" +
                "模型包文件: <input type=\"file\" name=\"modelPackage\" /><br/>" +
                "</div>" +

                "<div id=\"docker-section\" style=\"display:none;\">" +
                "Docker镜像: <input type=\"file\" name=\"dockerFile\" /><br/>" +
                "</div>" +

                "<input type=\"submit\" value=\"提交\" />" +

                "<script>" +
                "document.querySelectorAll('input[name=\"submitType\"]').forEach(radio => {" +
                "  radio.addEventListener('change', (e) => {" +
                "    document.getElementById('model-section').style.display = e.target.value === 'MODEL' ? 'block' : 'none';" +
                "    document.getElementById('docker-section').style.display = e.target.value === 'DOCKER' ? 'block' : 'none';" +
                "  });" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("submitType") String submitType, // 提交类型，包括 "MODEL","DOCKER"两种
            @RequestParam("modelPackage") MultipartFile modelFile,
            @RequestParam("dockerFile") MultipartFile dockerFile) {

        MultipartFile targetFile = submitType.equals("MODEL") ? modelFile : dockerFile;
        if (targetFile.isEmpty()) {
            return "文件为空，请重新选择文件上传。";
        }

        try {
            return submissionService.saveUploadedFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败：" + e.getMessage();
        }
    }
}
