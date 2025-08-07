package com.ecode.modelevalplat.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.SortedMap;

@Slf4j
@Component
public class RequestSignatureUtil {

    @Value("${app.security.sign.secret:default_secret}")
    private String secretKey;

    private static final long MAX_TIMESTAMP_DIFF = 180000; // 3分钟

    /**
     * 生成请求签名
     * @param params 请求参数
     * @param timestamp 时间戳
     * @return 签名
     */
    public String generateSignature(SortedMap<String, String> params, long timestamp) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1); // 删除最后一个&
            }

            sb.append("&timestamp=").append(timestamp);
            sb.append("&secret=").append(secretKey);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成签名失败", e);
            return null;
        }
    }

    /**
     * 验证请求签名
     * @param params 请求参数
     * @param timestamp 时间戳
     * @param signature 签名
     * @return 是否验证通过
     */
    public boolean verifySignature(SortedMap<String, String> params, long timestamp, String signature) {
        // 检查时间戳有效性（防止重放攻击）
        long currentTimestamp = Instant.now().toEpochMilli();
        if (Math.abs(currentTimestamp - timestamp) > MAX_TIMESTAMP_DIFF) {
            log.warn("请求时间戳超出允许范围，current: {}, request: {}", currentTimestamp, timestamp);
            return false;
        }

        String expectedSignature = generateSignature(params, timestamp);
        return expectedSignature != null && expectedSignature.equals(signature);
    }
}