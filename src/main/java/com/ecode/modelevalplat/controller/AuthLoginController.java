package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.dto.*;
import com.ecode.modelevalplat.service.AuthLoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthLoginController {
    private final AuthLoginService authLoginService;

    public AuthLoginController(AuthLoginService authLoginService) {
        this.authLoginService = authLoginService;
    }

    @PostMapping("/captcha")
    public ResVo<CaptchaResponseDTO> getCaptcha(@RequestBody @Valid CaptchaRequestDTO request) {
        return authLoginService.generateCaptcha(request.getUuid());
    }

    @PostMapping("/login-email-password")
    public ResVo<JwtResponseDTO> loginByEmailAndPassword(@RequestBody @Valid AuthLoginRequestDTO1 authLoginRequestDTO1,
                                                         HttpServletRequest httpRequest) {
        return authLoginService.loginByEmailAndPassword(authLoginRequestDTO1, httpRequest);
    }
    @PostMapping("/login-email-code")
    public ResVo<JwtResponseDTO> loginByEmailCode(@RequestBody @Valid AuthLoginRequestDTO2 authLoginRequestDTO2, HttpServletRequest httpRequest){
        return authLoginService.loginByEmailCode(authLoginRequestDTO2, httpRequest);
    }

    @PostMapping("/logout")
    public ResVo<Void> logout(HttpServletRequest request) {
        // 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResVo.fail(StatusEnum.USER_NOT_LOGGED_IN);
        }
        token = token.substring(7);

        return authLoginService.logout(token);
    }
}
