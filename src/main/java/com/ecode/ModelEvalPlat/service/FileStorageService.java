package com.ecode.ModelEvalPlat.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // 注入配置文件中的上传路径
    public FileStorageService(@Value("${model.upload.path}") String uploadPath) {
        this.fileStorageLocation = Paths.get(uploadPath)
                .toAbsolutePath().normalize();

        // 创建目录（如果不存在）
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建上传目录", ex);
        }
    }

    // 保存PTH模型文件
    public String storePthFile(MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pth")) {
            throw new RuntimeException("仅支持.pth格式的模型文件");
        }

        // 生成唯一文件名（避免冲突）
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        try {
            // 保存文件到目标路径
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("保存文件失败: " + fileName, ex);
        }
    }
}