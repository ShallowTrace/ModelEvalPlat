package com.ecode.modelevalplat.common;

import lombok.Getter;

/**
 * 异常码规范：
 * xxx - xxx - xxx
 * 业务 - 状态 - code
 * <p>
 * 业务取值
 * - 100 全局
 * - 200 文章相关
 * - 300 评论相关
 * - 400 用户相关
 * <p>
 * 状态：基于http status的含义
 * - 4xx 调用方使用姿势问题
 * - 5xx 服务内部问题
 * <p>
 * code: 具体的业务code
 *
 * @author JingYalin
 * @date 2025/7/22
 */
@Getter
public enum StatusEnum {

    // -------------------------------- 通用

    // 成功类
    REGISTER_SUCCESS(100_201_001, "注册成功"),
    VERIFY_CODE_SENT(100_200_002, "验证码发送成功"),
    VERIFY_CODE_VALID(100_200_003, "验证码校验通过"),

    // 用户错误类
    USER_ALREADY_EXISTS(100_409_004, "用户名已存在: %s"),
    EMAIL_ALREADY_EXISTS(100_409_005, "邮箱已存在: %s"),
    INVALID_EMAIL_FORMAT(100_400_006, "邮箱格式不正确: %s"),
    PASSWORD_WEAK(100_422_007, "密码不符合要求"),
    USERNAME_WEAK(100_400_008, "用户名不符合要求"),
    PASSWORD_TWICE(100_400_009, "两次输入密码不一致"),

    // 验证码TWICE
    VERIFY_CODE_EXPIRED(100_4_010, "验证码已过期"),
    VERIFY_CODE_INCORRECT(100_400_011, "验证码错误"),
    VERIFY_CODE_NOT_SENT(100_400_012, "请先请求验证码"),
    VERIFY_CODE_TOO_FREQUENT(100_429_013, "验证码请求频繁，请间隔2分钟发送一次"),
    MAX_VERIFY_CODE_REQUESTS_PER_ACCOUNT_PER_DAY(100_429_014, "该账号今日验证码发送次数上限！"),

    // 注册失败（兜底）
    REGISTER_FAILED(100_500_015, "注册失败，请稍后重试"),

    //系统繁忙
    SYSTEM_BUSY(100_503_016, "系统繁忙，请稍后重试"),
    IP_RATE_LIMIT_EXCEEDED(100_429_017, "今日发送验证码次数上限"),
    SYSTEM_ABNORMALITY(100_500_018, "系统异常，请稍后重试");

    private final int code;

    private final String msg;

    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
