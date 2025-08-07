package com.ecode.modelevalplat.util;

import java.util.UUID;

public class UUIDUtil {

    /**
     * 生成一个无横线的UUID字符串
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}