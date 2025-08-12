package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    // 密码一般不会在响应中返回，可以去掉或标注@JsonIgnore

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotNull(message = "激活状态不能为空")
    private Boolean isActive;

    private String gender;  // 可为空不校验

    private String avatarUrl;  // 可为空不校验

    private String department;  // 可为空不校验

    @NotNull(message = "注册时间不能为空")
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;  // 可为空不校验
}

