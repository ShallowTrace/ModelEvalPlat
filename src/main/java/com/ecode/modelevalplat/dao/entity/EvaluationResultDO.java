package com.ecode.modelevalplat.dao.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResultDO {
    private Long id;
    private Long userId;
    private Long competitionId;
    private Date submitTime;
    private String resultJson;
    private Float score;
}