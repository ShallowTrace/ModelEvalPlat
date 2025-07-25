package com.ecode.modelevalplat.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface AuthRegisterMapper extends BaseMapper<UserDO> {
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT * FROM users WHERE email = #{email} AND is_active = 0")
    UserDO getUnactivatedUserByEmail(String email);

    @Update("UPDATE users SET is_active = 1 WHERE email = #{email} AND is_active = 0")
    int activateUserByEmail(String email);

    void deleteUnactivatedUsersBefore(@Param("threshold") LocalDateTime threshold);
}
