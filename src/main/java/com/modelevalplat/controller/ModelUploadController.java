package com.modelevalplat.controller;

import com.modelevalplat.model.ModelEvaluationResult;
import com.modelevalplat.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ModelUploadController {

    private final ModelService modelService;

    @Autowired
    public ModelUploadController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/api/upload")
    public ResponseEntity<String> uploadModel(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = modelService.uploadModel(file);
            return ResponseEntity.ok("模型上传成功，保存路径：" + filePath);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("模型上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/api/evaluate")
    public ResponseEntity<ModelEvaluationResult> evaluateModel(@RequestParam("modelPath") String modelPath) {
        try {
            ModelEvaluationResult evaluationResult = modelService.evaluateModel(modelPath);
            return ResponseEntity.ok(evaluationResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}