package com.ecode.modelevalplat.controller;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.UserFieldUpdateDTO;
import com.ecode.modelevalplat.dto.UserQueryDTO;
import com.ecode.modelevalplat.dto.UserResponseDTO;
import com.ecode.modelevalplat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ResVo<UserResponseDTO> getUserById(@RequestBody @Valid UserQueryDTO userQueryDTO) {
        return userService.getUserById(userQueryDTO);
    }

    /**
     * 单字段更新用户信息
     */
    @PostMapping("/updateField")
    public ResVo<Void> updateUserField(@RequestBody @Valid UserFieldUpdateDTO userFieldUpdateDTO) {
        return userService.updateUserField(userFieldUpdateDTO);
    }
}
