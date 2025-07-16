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

    // 固定存储路径（可以根据需要修改）
    private static final String FILE_STORAGE_PATH = "C:\\Users\\ShallowTrace\\Desktop\\submission\\";

    @GetMapping("/upload")
    public String showUploadForm() {
        return "<html>" +
                "<body>" +
                "<form method=\"POST\" action=\"/api/submission/upload\" enctype=\"multipart/form-data\">" +
                "选择文件: <input type=\"file\" name=\"file\" /><br/>" +
                "<input type=\"submit\" value=\"提交\" />" +
                "</form>" +
                "</body>" +
                "</html>";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "文件为空，请重新选择文件上传。";
        }

        try {
            // 创建目标文件夹（如果不存在）
            File storageDir = new File(FILE_STORAGE_PATH);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            // 构建目标文件路径
            File dest = new File(FILE_STORAGE_PATH + file.getOriginalFilename());

            // 保存文件到指定路径
            file.transferTo(dest);

            return "文件上传成功：" + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败：" + e.getMessage();
        }
    }
}
