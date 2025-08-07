package com.ecode.modelevalplat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "密码登陆请求DTO")
@Data
public class PasswordLoginRequestDTO extends CaptchaRequestDTO{
    @ApiModelProperty(value = "邮箱", required = true, example = "test@example.com")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @ApiModelProperty(value = "密码", required = true, example = "Abc123!@#")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "动态验证码", required = true, example = "123456")
    @NotBlank(message = "动态验证码不能为空")
    private String dynamicCode;
}

