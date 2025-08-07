package com.ecode.modelevalplat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "注册请求DTO")
@Data
public class AuthRegisterRequestDTO {

    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "邮箱", required = true, example = "test@example.com")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @ApiModelProperty(value = "密码", required = true, example = "Abc123!@#")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "确认密码", required = true, example = "Abc123!@#")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @ApiModelProperty(value = "邮箱验证码", required = true, example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
