package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.AuthRegisterRequestDTO;

import javax.servlet.http.HttpServletRequest;

public interface AuthRegisterService {

    /**
     * 注册用户
     * @param dto 注册请求参数
     */
    ResVo<String> register(AuthRegisterRequestDTO dto);

    /**
     * 发送邮箱验证码
     * @param email 目标邮箱
     * @param username 用户名
     */
    ResVo<String> sendVerifyCode(HttpServletRequest request, String username, String email, String password, String confirmPassword);
}

