package com.ecode.modelevalplat.dto;


import lombok.Data;

import java.util.Date;

@Data
public class HistoryDTO {
    private Long id;
    private Long userId;
    private Long competitionId;
    private Date submitTime;
    private String recordType;  // "SUBMISSION" 或 "EVALUATION"
    private String status;       // SUBMISSION专属字段
    private Float score;
}
