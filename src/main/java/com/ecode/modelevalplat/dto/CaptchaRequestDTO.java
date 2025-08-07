package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CaptchaRequestDTO {
    @NotBlank(message = "UUID不能为空")
    private String uuid;
}