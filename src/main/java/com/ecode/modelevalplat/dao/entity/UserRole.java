package com.ecode.modelevalplat.dao.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum UserRole {
    USER("user"),
    ADMIN("admin"); // 仅在枚举常量上使用 @EnumValue

    @EnumValue // 标记此字段为数据库存储值
    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}