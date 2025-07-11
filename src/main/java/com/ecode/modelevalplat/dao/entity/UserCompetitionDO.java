package com.ecode.modelevalplat.dao.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompetitionDO {
    private Long id;
    private Long userId;
    private Long competitionId;
    private Date joinedAt;
}