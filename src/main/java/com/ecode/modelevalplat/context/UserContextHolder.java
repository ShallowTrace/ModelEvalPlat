package com.ecode.modelevalplat.context;

public class UserContextHolder {

    // ThreadLocal 存放当前线程的 userId
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

    // 设置 userId
    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    // 获取 userId String
    public static String getUserId() {
        return userIdHolder.get();
    }

    // 清理 ThreadLocal，防止内存泄漏
    public static void clear() {
        userIdHolder.remove();
    }
}

