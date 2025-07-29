package com.ecode.modelevalplat.dao.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDO {
    private Long id;
    private Long userId;
    private Long competitionId;
    private String modelPath;
    private String status; // 实际应为枚举类型
    private Date submitTime;
}