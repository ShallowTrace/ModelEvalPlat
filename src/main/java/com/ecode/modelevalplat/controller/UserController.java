package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.context.UserContextHolder;
import com.ecode.modelevalplat.dto.UserFieldUpdateDTO;
import com.ecode.modelevalplat.dto.UserResponseDTO;
import com.ecode.modelevalplat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID获取用户信息
     */
    @PostMapping("/id")
    public ResVo<UserResponseDTO> getUserInfo() {
        String userId = UserContextHolder.getUserId();
        return userService.getUserById(userId);
    }

    /**
     * 单字段更新用户信息
     */
    @PostMapping("/updateField")
    public ResVo<Void> updateUserField(@RequestBody @Valid UserFieldUpdateDTO userFieldUpdateDTO) {
        return userService.updateUserField(userFieldUpdateDTO);
    }
}
