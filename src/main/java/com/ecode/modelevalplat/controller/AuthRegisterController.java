package com.ecode.modelevalplat.controller;


import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.AuthCompleteRegisterDTO;
import com.ecode.modelevalplat.dto.AuthPreRegisterDTO;
import com.ecode.modelevalplat.service.AuthRegisterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

//@RestController
//@RequestMapping("/api/auth")
//public class AuthRegisterController {
//    private final AuthRegisterService authRegisterService;
//
//    @Autowired
//    public AuthRegisterController(AuthRegisterService authRegisterService) {
//        this.authRegisterService = authRegisterService;
//    }
//
//    @PostMapping("/register")
//    public ResVo<String> register(@RequestBody AuthRegisterRequestDTO dto) {
//        try {
//            authRegisterService.register(dto);
//            // 返回注册成功状态码
//            return ResVo.ok(StatusEnum.REGISTER_SUCCESS.getMsg());
//        } catch (RuntimeException e) {
//            // 返回失败，带失败消息
//            return ResVo.fail(StatusEnum.REGISTER_FAILED, e.getMessage());
//        }
//    }
//}
// 控制器：接收前端请求并调用服务层
@RestController
@RequestMapping("/auth")
public class AuthRegisterController {

    private final AuthRegisterService authRegisterService;

    public AuthRegisterController(AuthRegisterService authRegisterService) {
        this.authRegisterService = authRegisterService;
    }

    /** 预注册接口 */
    @PostMapping("/pre-register")
    public ResVo<String> preRegister(HttpServletRequest httpServletRequest, @RequestBody @Valid AuthPreRegisterDTO authPreRegisterDTO) {
        // 调用服务层预注册方法
        return authRegisterService.preRegister(httpServletRequest, authPreRegisterDTO);
    }

    @PostMapping("/complete-register")
    public ResVo<String> completeRegister(@RequestBody @Valid AuthCompleteRegisterDTO authCompleteRegisterDTO) {
        // 调用服务层完成注册方法
        return authRegisterService.completeRegister(authCompleteRegisterDTO);
    }


}

