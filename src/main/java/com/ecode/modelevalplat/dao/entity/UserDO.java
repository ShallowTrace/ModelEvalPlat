package com.ecode.modelevalplat.dao.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    private Long id;
    private String username;
    private String password;
    private UserRole role; // 实际应为枚举类型
    private Date createdAt;
    private Date lastLoginAt;
}