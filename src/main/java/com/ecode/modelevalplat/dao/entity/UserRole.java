package com.ecode.modelevalplat.dao.entity;

// UserRole.java（枚举类）
public enum UserRole {
    USER("user"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

}