package com.ecode.modelevalplat.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private String path;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("participant_count")
    private Integer participantCount;

    @JsonProperty("daily_submission_limit")
    private Integer dailySubmissionLimit;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    // 新增注册时间字段
    @JsonProperty("register_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private Date registerStartTime;

    @JsonProperty("register_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private Date registerEndTime;
}