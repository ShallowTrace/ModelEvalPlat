package com.ecode.modelevalplat.controller;


import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.AuthRegisterRequestDTO;
import com.ecode.modelevalplat.service.AuthRegisterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
@RequestMapping("/api/auth")
public class AuthRegisterController {

    private final AuthRegisterService authRegisterService;

    public AuthRegisterController(AuthRegisterService authRegisterService) {
        this.authRegisterService = authRegisterService;
    }

    /** 发送验证码接口 */
    @PostMapping("/send-code")
    public ResVo<String> sendCode(HttpServletRequest request, @RequestBody AuthRegisterRequestDTO req) {
        // 调用服务层发送验证码
        ResVo<String> result = authRegisterService.sendVerifyCode(
                request,
                req.getUsername(),
                req.getEmail(),
                req.getPassword(),
                req.getConfirmPassword());
        return result;
    }

    /** 注册接口 */
    @PostMapping("/register")
    public ResVo<String> register(@RequestBody AuthRegisterRequestDTO dto) {
        // 调用服务层注册方法
        ResVo<String> result = authRegisterService.register(dto);
        return result;
    }
}

