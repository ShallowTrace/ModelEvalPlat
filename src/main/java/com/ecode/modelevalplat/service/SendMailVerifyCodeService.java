package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;

public interface SendMailVerifyCodeService {
    ResVo<String> sendEmailVerifyCode(String email);
}
