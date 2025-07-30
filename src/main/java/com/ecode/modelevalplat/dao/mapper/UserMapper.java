package com.ecode.modelevalplat.dao.mapper;


import com.ecode.modelevalplat.dao.entity.UserDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users(username, password, role, created_at, last_login_at) " +
            "VALUES(#{username}, #{password}, #{role}, #{createdAt}, #{lastLoginAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserDO user);

    @Update("UPDATE users SET username=#{username}, password=#{password}, role=#{role}, " +
            "last_login_at=#{lastLoginAt} WHERE id=#{id}")
    void update(UserDO user);

    @Delete("DELETE FROM users WHERE id=#{id}")
    void delete(Long id);

    @Select("SELECT * FROM users WHERE id=#{id}")
    UserDO findById(Long id);


    @Select("SELECT * FROM users WHERE username=#{username}")
    UserDO findByUsername(String username);


    @Select("SELECT * FROM users")
    List<UserDO> findAll();

}