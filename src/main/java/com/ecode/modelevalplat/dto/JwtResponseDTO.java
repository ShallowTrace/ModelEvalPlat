package com.ecode.modelevalplat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "JWT 登录响应 DTO")
public class JwtResponseDTO {

    @ApiModelProperty(value = "访问令牌（JWT）", example = "eyJhbGciOiJIUzUxMiIsInR...")
    private String token;

    @ApiModelProperty(value = "刷新令牌", example = "eyJhbGciOiJIUzUxMiIsInR...refresh...")
    private String refreshToken;

    @ApiModelProperty(value = "用户ID", example = "10001")
    private String userId;

    @ApiModelProperty(value = "用户名", example = "john_doe")
    private String username;

    @ApiModelProperty(value = "用户角色", example = "ADMIN")
    private String role;
}