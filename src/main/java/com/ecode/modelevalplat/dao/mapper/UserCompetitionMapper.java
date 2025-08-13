package com.ecode.modelevalplat.dao.mapper;

import com.ecode.modelevalplat.dao.entity.UserCompetitionDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserCompetitionMapper {
    @Insert("INSERT INTO user_competitions(user_id, competition_id, joined_at) " +
            "VALUES(#{userId}, #{competitionId}, #{joinedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserCompetitionDO userCompetition);


    @Delete("DELETE FROM user_competitions WHERE id=#{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM user_competitions WHERE user_id=#{userId} AND competition_id=#{competitionId}")
    void deleteByUserAndCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);

    @Select("SELECT * FROM user_competitions WHERE id=#{id}")
    UserCompetitionDO findById(Long id);


    @Select("SELECT * FROM user_competitions WHERE user_id=#{userId}")
    List<UserCompetitionDO> findByUserId(Long userId);


    @Select("SELECT * FROM user_competitions WHERE competition_id=#{competitionId}")
    List<UserCompetitionDO> findByCompetitionId(Long competitionId);


    @Select("SELECT * FROM user_competitions WHERE user_id=#{userId} AND competition_id=#{competitionId}")
    UserCompetitionDO findByUserAndCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);


}