package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.*;

import javax.servlet.http.HttpServletRequest;

public interface AuthLoginService {
    ResVo<JwtResponseDTO> loginByEmailAndPassword(AuthLoginRequestDTO1 authLoginRequestDTO1, HttpServletRequest httpRequest);
    ResVo<JwtResponseDTO> loginByEmailCode(AuthLoginRequestDTO2 authLoginRequestDTO2, HttpServletRequest httpRequest);
    ResVo<CaptchaResponseDTO> generateCaptcha(String uuid);

    ResVo<Void> logout(String token);
}