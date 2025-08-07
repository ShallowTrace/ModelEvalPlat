package com.ecode.modelevalplat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥（用于签名）
     */
    private String secret;

    /**
     * token 有效时间（单位：秒）
     */
    private long expiration;

    /**
     * refresh token 有效时间（单位：秒）
     */
    private long refreshExpiration;
}