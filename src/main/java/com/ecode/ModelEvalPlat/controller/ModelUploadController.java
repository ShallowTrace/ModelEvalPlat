package com.ecode.ModelEvalPlat.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecode.ModelEvalPlat.service.FileStorageService;

@RestController
@RequestMapping("/api/model")
public class ModelUploadController {

    private final FileStorageService fileStorageService;


    @Autowired
    public ModelUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // 处理PTH模型文件上传
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPthModel(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.storePthFile(file);
            return ResponseEntity.ok("模型文件上传成功: " + fileName);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("上传失败: " + ex.getMessage());
        }
    }
}