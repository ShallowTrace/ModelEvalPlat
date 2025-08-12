package com.ecode.modelevalplat.common.enums;

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
 */
@Getter
public enum StatusEnum {
    SUCCESS(0, "OK"),

    // -------------------------------- 通用

    // 注册模块
    REGISTER_SUCCESS(100_201_001, "注册成功"),
    VERIFY_CODE_SENT(100_200_002, "验证码发送成功"),
    VERIFY_CODE_VALID(100_200_003, "验证码校验通过"),
    USER_ALREADY_EXISTS(100_409_004, "用户名已存在: %s"),
    EMAIL_ALREADY_EXISTS(100_409_005, "邮箱已存在: %s"),
    INVALID_EMAIL_FORMAT(100_400_006, "邮箱格式不正确: %s"),
    PASSWORD_WEAK(100_422_007, "密码不符合要求"),
    USERNAME_WEAK(100_400_008, "用户名不符合要求"),
    PASSWORD_TWICE(100_400_009, "两次输入密码不一致"),
    VERIFY_CODE_EXPIRED(100_400_010, "验证码已过期"),
    VERIFY_CODE_INCORRECT(100_400_011, "验证码错误"),
    VERIFY_CODE_NOT_SENT(100_400_012, "请先请求验证码"),
    VERIFY_CODE_TOO_FREQUENT(100_429_013, "验证码请求频繁，请间隔2分钟发送一次"),
    MAX_VERIFY_CODE_REQUESTS_PER_ACCOUNT_PER_DAY(100_429_014, "该账号今日验证码发送次数上限！"),
    REGISTER_FAILED(100_500_015, "注册失败，请稍后重试"),
    SYSTEM_BUSY(100_503_016, "系统繁忙，请稍后重试"),
    IP_RATE_LIMIT_EXCEEDED(100_429_017, "今日发送验证码次数上限"),
    SYSTEM_ABNORMALITY(100_500_018, "系统异常，请稍后重试"),
    TOO_MANY_USERS_REGISTER_IN_FROM_IP(200_401_016, "当前IP登录注册数量已达上限，请稍后再试"),


    //登陆模块
    WRONG_PASSWORD(200_401_001, "密码输入错误"),
    TEMP_LOCKED_TOO_MANY_PASSWORD_FAILURES(200_401_002, "密码输入错误次数过多，请30分钟后再试"),
    TOO_MANY_WRONG_PASSWORDS(200_401_003, "密码输入错误次数过多，请明天再登录或使用邮箱验证登录"),
    USER_NOT_FOUND(200_401_004, "用户（邮箱）不存在"),
    LOGIN_TOO_FREQUENT(200_401_005, "登录过于频繁，请1小时后重试"),
    ACCOUNT_LOGGED_IN_ELSEWHERE(200_401_006, "该账号已在其他地方登录，请稍后重试"),
    ACCOUNT_LOCKED_FOR_TODAY(200_401_007, "账户已被锁定，今日不能再登录"),
    EMAIL_CODE_SENT_SUCCESS(200_200_008, "邮箱验证码发送成功"),
    EMAIL_CODE_EXPIRED(200_401_009, "邮箱验证码已过期"),
    LOGIN_INVALID_EMAIL_FORMAT(200_400_010, "邮箱格式不正确"),
    EMAIL_CODE_INVALID(200_401_011, "邮箱验证码错误"),
    DYNAMIC_CODE_INVALID(200_401_012, "动态验证码错误"),
    DYNAMIC_CODE_GENERATE(200_401_013, "动态验证码生成成功"),
    PLEASE_REQUEST_CODE_FIRST(200_401_014, "请先请求发送邮箱验证码"),
    EMAIL_CODE_SEND_TOO_FREQUENT(200_401_015, "邮箱验证码发送频繁，请2分钟后再试"),
    TOO_MANY_USERS_LOGGED_IN_FROM_IP(200_401_016, "当前IP登录用户数量已达上限，请稍后再试"),


    // 提交相关异常类型，前缀为300
    SUBMISSION_FAILED_MIXED(300_400_001, "提交失败:%s"),



    // 评估相关异常，前缀为500
    EVALUATION_FAILED_MIXED(500_400_001, "评估失败:%s"),

    //系统异常
    FORBID_ERROR(600_403_001, "无权限"),

    FORBID_ERROR_MIXED(600_403_002, "无权限:%s"),
    FORBID_NOTLOGIN(600_403_003, "未登录"),

    // 全局，数据不存在
    RECORDS_NOT_EXISTS(600_404_001, "记录不存在:%s"),

    // 系统异常
    UNEXPECT_ERROR(600_500_001, "非预期异常:%s"),

    // 图片相关异常类型
    UPLOAD_PIC_FAILED(600_500_002, "图片上传失败！"),
    // 文件存储异常，IOException
    SAVE_FILE_FAILED(600_500_003, "文件存储失败:%s"),

    // 比赛报名模块 (700前缀)
    COMPETITION_REGISTRATION_SUCCESS(700_500_001, "比赛报名成功"),
    COMPETITION_NOT_FOUND(700_500_002, "比赛不存在"),
    COMPETITION_INACTIVE(700_500_003, "比赛未激活"),
    COMPETITION_NOT_STARTED(700_500_004, "比赛尚未开始"),
    COMPETITION_ENDED(700_500_005, "比赛已结束"),
    ALREADY_REGISTERED(700_500_006, "用户已报名该比赛"),
    REGISTRATION_SYSTEM_ERROR(700_500_007, "报名系统异常");

    private final int code;

    private final String msg;

    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static boolean is5xx(int code) {
        return code % 1000_000 / 1000 >= 500;
    }

    public static boolean is403(int code) {
        return code % 1000_000 / 1000 == 403;
    }

    public static boolean is4xx(int code) {
        return code % 1000_000 / 1000 < 500;
    }
}
