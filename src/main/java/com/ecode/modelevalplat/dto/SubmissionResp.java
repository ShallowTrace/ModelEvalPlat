package com.ecode.modelevalplat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SubmissionResp {
    private boolean success;      // 是否处理成功
    private String message;       // 提示消息
    private Long submissionId;    // 提交记录ID（成功时）
    private String status;        // 提交状态
    private String errorCode;     // 错误代码（可选）
    private String originalFilename; // 原始文件名
    private Instant submitTime;   // 提交时间


    // 成功静态工厂方法
    public static SubmissionResp success(Long submissionId, String originalFilename) {
        return new SubmissionResp(
                true,
                "提交成功，等待评测",
                submissionId,
                "PENDING",
                null,
                originalFilename,
                Instant.now()
        );
    }

    // 失败静态工厂方法
    public static SubmissionResp failure(String message, String errorCode, String originalFilename) {
        return new SubmissionResp(
                false,
                message,
                null,
                "FAILED",
                errorCode,
                originalFilename,
                Instant.now()
        );
    }
}