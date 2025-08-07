package com.ecode.modelevalplat.util;

import java.util.regex.Pattern;

public class PasswordUtils {

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?].*");

    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) return false;

        int count = 0;
        if (UPPER.matcher(password).find()) count++;
        if (LOWER.matcher(password).find()) count++;
        if (DIGIT.matcher(password).find()) count++;
        if (SPECIAL.matcher(password).find()) count++;

        // 至少包含三种字符类型，且不能连续相同字符超过 4 个
        return count >= 3 && !hasRepeatedChars(password, 4);
    }

    private static boolean hasRepeatedChars(String password, int limit) {
        char last = 0;
        int repeat = 1;
        for (char c : password.toCharArray()) {
            if (c == last) {
                repeat++;
                if (repeat > limit) return true;
            } else {
                repeat = 1;
            }
            last = c;
        }
        return false;
    }
}