package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.dao.entity.UserDO;
import com.ecode.modelevalplat.dao.mapper.UserMapper;
import com.ecode.modelevalplat.dto.UserFieldUpdateDTO;
import com.ecode.modelevalplat.dto.UserResponseDTO;
import com.ecode.modelevalplat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.baomidou.mybatisplus.extension.toolkit.Db.updateById;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResVo<UserResponseDTO> getUserById(String userId) {

        UserDO userDO = userMapper.findById(Long.valueOf(userId));
        if (userDO == null) {
            return ResVo.fail(StatusEnum.USER_INFO_NOT_FOUND);
        }
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userDO.getId().toString());
        userResponseDTO.setUsername(userDO.getUsername());
        userResponseDTO.setEmail(userDO.getEmail());
        userResponseDTO.setRole(userDO.getRole());
        userResponseDTO.setIsActive(userDO.getIsActive());
        userResponseDTO.setGender(userDO.getGender());
        userResponseDTO.setAvatarUrl(userDO.getAvatarUrl());
        userResponseDTO.setDepartment(userDO.getDepartment());
        userResponseDTO.setLastLoginAt(userDO.getLastLoginAt());
        userResponseDTO.setCreatedAt(userDO.getCreatedAt());

        return ResVo.ok(StatusEnum.USER_INFO_SUCCESS,userResponseDTO);
    }

    @Override
    public ResVo<Void> updateUserField(UserFieldUpdateDTO userFieldUpdateDTO) {
        UserDO user = userMapper.selectById(userFieldUpdateDTO.getId());

        String field = userFieldUpdateDTO.getFieldName();
        String value = userFieldUpdateDTO.getFieldValue();

        switch (field) {
            case "username":
                if (value == null || value.trim().isEmpty()) {
                    return ResVo.fail(StatusEnum.USERNAME_EMPTY);
                }
                if (value.equals(userMapper.findByUsername(value).getUsername())) {
                    return ResVo.fail(StatusEnum.USERNAME_EXISTS);
                }
                user.setUsername(value);
                break;

            case "password":
                if (value == null || value.trim().isEmpty()) {
                    return ResVo.fail(StatusEnum.PASSWORD_EMPTY);
                }
                if (userFieldUpdateDTO.getOldPassword() == null || !passwordEncoder.matches(userFieldUpdateDTO.getOldPassword(), user.getPassword())) {
                    return ResVo.fail(StatusEnum.OLD_PASSWORD_ERROR);
                }
                user.setPassword(passwordEncoder.encode(value));
                break;

            case "gender":
                // 可加性别合法性校验
                user.setGender(value);
                break;

            case "department":
                user.setDepartment(value);
                break;

            default:
                return ResVo.fail(StatusEnum.INVALID_FIELD);
        }

        if (!updateById(user)) {
            return ResVo.fail(StatusEnum.UPDATE_USER_INFO_FAILED);
        }

        return ResVo.ok(StatusEnum.UPDATE_USER_INFO_SUCCESS);
    }
}
