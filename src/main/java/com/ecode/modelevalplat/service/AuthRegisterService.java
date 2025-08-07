package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.AuthCompleteRegisterDTO;

import javax.servlet.http.HttpServletRequest;

public interface AuthRegisterService {

    ResVo<String> preRegister(HttpServletRequest httpServletRequest, String email);




    ResVo<String> completeRegister(AuthCompleteRegisterDTO authCompleteRegisterDTO);
}

