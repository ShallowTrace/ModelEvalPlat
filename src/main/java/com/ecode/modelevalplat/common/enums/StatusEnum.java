package com.ecode.modelevalplat.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 提交状态枚举类
 */
public enum StatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),
    /**
     * 成功
     */
    SUCCESS(2, "成功"),
    /**
     * 失败
     */
    FAILED(3, "失败");

    /**
     * 数据库存储值
     */
    @EnumValue
    private final int code;

    /**
     * 前端显示值
     */
    @JsonValue
    private final String desc;

    StatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static StatusEnum getByCode(int code) {
        for (StatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据desc获取枚举
     */
    public static StatusEnum getByDesc(String desc) {
        for (StatusEnum status : values()) {
            if (status.desc.equals(desc)) {
                return status;
            }
        }
        return null;
    }
}