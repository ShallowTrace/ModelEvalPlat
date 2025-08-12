package com.ecode.modelevalplat.service;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.dto.UserFieldUpdateDTO;
import com.ecode.modelevalplat.dto.UserQueryDTO;
import com.ecode.modelevalplat.dto.UserResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResVo<UserResponseDTO> getUserById(UserQueryDTO userQueryDTO);

//    int countByUsername(String username);
//
//    boolean updateById(UserDO user);
//
    ResVo<Void> updateUserField(UserFieldUpdateDTO userFieldUpdateDTO);
}
