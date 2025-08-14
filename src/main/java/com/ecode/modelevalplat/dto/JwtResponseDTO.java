package com.ecode.modelevalplat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "JWT 登录响应 DTO")
public class JwtResponseDTO {

    @ApiModelProperty(value = "访问令牌（JWT）", example = "eyJhbGciOiJIUzUxMiIsInR...")
    private String token;

//    @ApiModelProperty(value = "刷新令牌", example = "eyJhbGciOiJIUzUxMiIsInR...refresh...")
//    private String refreshToken;

}