package com.ecode.modelevalplat.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthLoginMapper extends BaseMapper<UserDO> {

    @Select("SELECT * FROM users WHERE email = #{email}")
    UserDO selectByEmail(String email);

    @Update("UPDATE users SET last_login_at = NOW() WHERE id = #{userId}")
    void updateLastLoginTime(Long userId);
}