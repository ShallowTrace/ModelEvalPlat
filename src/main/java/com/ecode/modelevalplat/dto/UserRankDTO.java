package com.ecode.modelevalplat.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserRankDTO {
    private Integer rank;          // 排名
    private Long userId;           // 用户ID
    private String username;       // 用户名
    private double primaryScore;   // 主评分指标（如准确率）
    private Date firstSubmitTime;  // 最好成绩的最早提交时间
}
