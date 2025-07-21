package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCompetition {
    private Long id;
    private Long userId;
    private Long competitionId;
    private LocalDateTime joinedAt;
}