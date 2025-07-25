package com.ecode.modelevalplat.dao.mapper;

import com.ecode.modelevalplat.dao.entity.SubmissionDO;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface SubmissionMapper {
    @Insert("INSERT INTO submissions(user_id, competition_id, model_path, status, submit_time) " +
            "VALUES(#{userId}, #{competitionId}, #{modelPath}, #{status}, #{submitTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SubmissionDO submission);


    @Update("UPDATE submissions SET status=#{status} WHERE id=#{id}")
    void updateStatus(SubmissionDO submission);


    @Delete("DELETE FROM submissions WHERE id=#{id}")
    void delete(Long id);

    @Select("SELECT * FROM submissions WHERE id=#{id}")
    SubmissionDO findById(Long id);


    @Select("SELECT * FROM submissions WHERE user_id=#{userId}")
    List<SubmissionDO> findByUserId(Long userId);

    @Select("SELECT * FROM submissions WHERE competition_id=#{competitionId}")
    List<SubmissionDO> findByCompetitionId(Long competitionId);

    @Select("SELECT * FROM submissions WHERE user_id = #{userId} AND competition_id = #{competitionId} ORDER BY submit_time DESC")
    List<SubmissionDO> findByUserAndCompetition(
            @Param("userId") Long userId,
            @Param("competitionId") Long competitionId
    );

    @Select("SELECT COUNT(*) FROM submissions WHERE user_id=#{userId} AND competition_id=#{competitionId} " +
            "AND DATE(submit_time) = CURRENT_DATE")
    int countTodaySubmissions(@Param("userId") Long userId, @Param("competitionId") Long competitionId);


}