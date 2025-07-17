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
                "<input type=\"radio' name='submitType' value='MODEL' checked> 模型提交（模型+脚本+环境）<br>" +
                "<input type=\"radio' name='submitType' value='DOCKER'> Docker镜像提交<br>" +
                "<div id='model-files'>" +
                "模型文件: <input type=\"file\" name=\"modelFile\" /><br/>" +
                "推理脚本: <input type=\"file\" name=\"scriptFile\" /><br/>" +
                "环境配置: <input type=\"file\" name=\"envFile\" /><br/>" +
                "</div>" +
                "<div id='docker-file' style='display:none'>" +
                "Docker镜像: <input type=\"file\" name=\"dockerFile\" /><br/>" +
                "</div>" +
                "<input type=\"submit\" value=\"提交\" />" +
                "</form>" +
                "<script>" +
                "document.querySelectorAll('input[name=\"submitType\"]').forEach(radio => {" +
                "  radio.addEventListener('change', (e) => {" +
                "    document.getElementById('model-files').style.display = e.target.value === 'MODEL' ? 'block' : 'none';" +
                "    document.getElementById('docker-file').style.display = e.target.value === 'DOCKER' ? 'block' : 'none';" +
                "  })" +
                "})" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "文件为空，请重新选择文件上传。";
        }

        try {
            return submissionService.saveUploadedFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败：" + e.getMessage();
        }
    }
}
