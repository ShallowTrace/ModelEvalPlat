package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthCompleteRegisterDTO {
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "动态验证码不能为空")
    private String dynamicCode;

    @NotBlank(message = "uuid不能为空")
    private String uuid;
}
