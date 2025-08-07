package com.ecode.modelevalplat.dto;

import lombok.Data;

@Data
public class CaptchaResponseDTO {
    private String uuid;
    private String imageBase64;
}
