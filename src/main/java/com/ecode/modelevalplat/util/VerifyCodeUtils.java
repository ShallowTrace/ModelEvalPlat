package com.ecode.modelevalplat.util;

import java.security.SecureRandom;


public class VerifyCodeUtils {

    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成 6 位数字验证码
     * @return 6位验证码字符串，例如 "483921"
     */
    public static String generate6DigitCode() {
        int code = 100000 + random.nextInt(900000); // 保证是 6 位数字
        return String.valueOf(code);
    }
}
