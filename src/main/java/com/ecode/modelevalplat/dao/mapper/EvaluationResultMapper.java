package com.ecode.modelevalplat.dao.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecode.modelevalplat.dao.entity.EvaluationResultDO;
import com.ecode.modelevalplat.dto.UserRankDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationResultMapper extends BaseMapper<EvaluationResultDO> {
    // 获取比赛排行榜，首先按照得分降序，如有并列，再按照最高得分的首次提交时间升序
    @Select("SELECT u.id AS user_id, u.username, max_scores.max_score AS primary_score, MIN(er.submit_time) AS first_submit_time " +
            "FROM users u " +
            "JOIN (" +
            "    SELECT user_id, MAX(score) AS max_score" +
            "    FROM evaluation_results" +
            "    WHERE competition_id = #{competitionId}" +
            "    GROUP BY user_id" +
            ") max_scores ON u.id = max_scores.user_id " +
            "JOIN evaluation_results er ON er.user_id = max_scores.user_id AND er.competition_id = #{competitionId} AND er.score = max_scores.max_score " +
            "GROUP BY u.id, u.username, max_scores.max_score " +
            "ORDER BY primary_score DESC, first_submit_time ASC")
    List<UserRankDTO> selectCompetitionRank(@Param("competitionId") Long competitionId);
}