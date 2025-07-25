package com.ecode.modelevalplat.util;

import javax.servlet.http.HttpServletRequest;

public class IpUtils {

    /**
     * 获取客户端真实IP地址，兼容本地开发和Nginx代理后的线上环境
     */
    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 如果有多个 IP，用第一个
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr(); // fallback: 本地或无代理
    }
}