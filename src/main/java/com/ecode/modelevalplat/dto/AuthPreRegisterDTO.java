package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthPreRegisterDTO {
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
