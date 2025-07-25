package com.ecode.modelevalplat.util;

import java.util.regex.Pattern;

public class UsernameUtils {

    // 用户名必须是字母开头，长度 5~20，不能含有特殊字符
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,19}$");

    /**
     * 验证用户名是否符合规范
     * @param username 待验证的用户名
     * @return 符合规范返回true，否则返回false
     */
    public static boolean isValid(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
}