package com.ecode.modelevalplat.dao.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDO {
    private Long id;
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;
    private String path;
    private Boolean isActive;
    private Integer participantCount;
    private Integer dailySubmissionLimit;
    private Date createdAt;
}