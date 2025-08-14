package com.ecode.modelevalplat.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserDO {

    private Long id;

    private String username;

    private String password;

    private String email;

    private String role;  // 可以写成枚举类，数据库是 ENUM('user','admin')

    private Boolean isActive;

    private String gender;  // male, female, other

    private String avatarUrl;

    private String department;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;
}