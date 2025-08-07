package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.sendEmailVerifyCodeDTO;
import com.ecode.modelevalplat.service.SendMailVerifyCodeService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SendMailVerifyCodeController {
    private final SendMailVerifyCodeService sendMailVerifyCodeService;


    public SendMailVerifyCodeController(SendMailVerifyCodeService sendMailVerifyCodeService) {
        this.sendMailVerifyCodeService = sendMailVerifyCodeService;
    }

    @RequestMapping("/send-code")
    public ResVo<String> sendEmailVerifyCode(@RequestBody sendEmailVerifyCodeDTO sendEmailVerifyCodeDTO){
        return sendMailVerifyCodeService.sendEmailVerifyCode(sendEmailVerifyCodeDTO.getEmail());
    }

}
