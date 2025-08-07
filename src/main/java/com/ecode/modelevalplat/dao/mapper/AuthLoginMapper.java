package com.ecode.modelevalplat.dao.mapper;

import com.ecode.modelevalplat.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthLoginMapper {
    UserDO selectByEmail(String email);
    void updateLastLoginTime(Long userId);
}
