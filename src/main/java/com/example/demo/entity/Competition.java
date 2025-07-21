package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Competition {
    private Long id;

    private String name;

    private String description;

    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private String path;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("participant_count")
    private Integer participantCount;

    @JsonProperty("daily_submission_limit")
    private Integer dailySubmissionLimit;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}