package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class AuthLoginRequestDTO2 {


    @NotBlank(message = "邮箱不能为空")
    private String email;


    @NotBlank(message = "邮箱验证码不能为空")
    private String verifyCode;


    @NotBlank(message = "动态验证码不能为空")
    private String dynamicCode;


    @NotBlank(message = "uuid不能为空")
    private String uuid;
}
