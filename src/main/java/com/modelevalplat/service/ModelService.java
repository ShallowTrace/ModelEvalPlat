package com.modelevalplat.service;

import com.modelevalplat.model.ModelEvaluationResult;
import org.springframework.web.multipart.MultipartFile;

public interface ModelService {
    String uploadModel(MultipartFile file) throws Exception;
    ModelEvaluationResult evaluateModel(String modelPath) throws Exception;
}