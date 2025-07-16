package com.ecode.modelevalplat.dao.mapper;

import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationResultMapper {
    @Insert("INSERT INTO evaluation_results(user_id, competition_id, submit_time, result_json, score) " +
            "VALUES(#{userId}, #{competitionId}, #{submitTime}, #{resultJson}, #{score})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EvaluationResultDO evaluationResult);


    @Delete("DELETE FROM evaluation_results WHERE id=#{id}")
    void delete(Long id);

    @Select("SELECT * FROM evaluation_results WHERE id=#{id}")
    EvaluationResultDO findById(Long id);


    @Select("SELECT * FROM evaluation_results WHERE user_id=#{userId}")
    List<EvaluationResultDO> findByUserId(Long userId);


    @Select("SELECT * FROM evaluation_results WHERE competition_id=#{competitionId}")
    List<EvaluationResultDO> findByCompetitionId(Long competitionId);


    @Select("SELECT * FROM evaluation_results WHERE user_id=#{userId} AND competition_id=#{competitionId}")
    List<EvaluationResultDO> findByUserAndCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);


    @Select("SELECT * FROM evaluation_results WHERE user_id=#{userId} AND competition_id=#{competitionId} " +
            "ORDER BY score DESC LIMIT 1")
    EvaluationResultDO findBestResultByUserAndCompetition(@Param("userId") Long userId, @Param("competitionId") Long competitionId);

}