package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.AuthCompleteRegisterDTO;
import com.ecode.modelevalplat.dto.AuthPreRegisterDTO;

import javax.servlet.http.HttpServletRequest;

public interface AuthRegisterService {

    ResVo<String> preRegister(HttpServletRequest httpServletRequest, AuthPreRegisterDTO authPreRegisterDTO);




    ResVo<String> completeRegister(AuthCompleteRegisterDTO authCompleteRegisterDTO);
}

