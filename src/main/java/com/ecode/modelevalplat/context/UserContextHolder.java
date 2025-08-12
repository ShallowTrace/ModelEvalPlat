package com.ecode.modelevalplat.context;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static void set(UserContext userContext) {
        CONTEXT.set(userContext);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // 可选：封装获取 ID、用户名、角色的方法
    public static String getUserId() {
        UserContext context = get();
        return context != null ? context.getUserId() : null;
    }

    public static String getUsername() {
        UserContext context = get();
        return context != null ? context.getUsername() : null;
    }

    public static String getRole() {
        UserContext context = get();
        return context != null ? context.getRole() : null;
    }
}
