package com.ecode.modelevalplat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRankDTO {
    private int rank;              // 排名（支持并列）
    private Long userId;           // 用户ID
    private String username;       // 用户名
    private String team;           // 所属团队（可选）
    private double primaryScore;   // 主评分指标（如准确率）
    private double secondaryScore; // 次评分指标（如耗时）
    private String bestSubmissionId; // 最佳提交ID
    private LocalDateTime lastSubmitTime; // 最后提交时间
}
