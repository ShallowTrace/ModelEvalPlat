package com.ecode.modelevalplat.dao.mapper;


import com.ecode.modelevalplat.dao.entity.CompetitionDO;
//import com.example.demo.entity.Competition;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CompetitionMapper {

    @Select("SELECT * FROM competitions")
    @Results(id = "competitionMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "path", column = "path"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "participantCount", column = "participant_count"),
            @Result(property = "dailySubmissionLimit", column = "daily_submission_limit"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CompetitionDO> selectAllCompetition();

    @Insert("INSERT INTO competitions (name, description, start_time, end_time, path, " +
            "is_active, participant_count, daily_submission_limit, created_at) " +
            "VALUES (#{name}, #{description}, #{startTime}, #{endTime}, #{path}, " +
            "#{isActive}, #{participantCount}, #{dailySubmissionLimit}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertCompetition(CompetitionDO competition);

    @Delete("DELETE FROM competitions WHERE id = #{id}")
    int deleteCompetition(Long id);

    @Update("UPDATE competitions SET description = #{description} WHERE id = #{competitionId}")
    int updateCompetitionDescription(@Param("competitionId") Long competitionId,
                                     @Param("description") String description);

    @Update("UPDATE competitions SET start_time = #{startTime} WHERE id = #{competitionId}")
    int updateCompetitionStartTime(@Param("competitionId") Long competitionId,
                                   @Param("startTime") LocalDateTime startTime);

    @Update("UPDATE competitions SET end_time = #{endTime} WHERE id = #{competitionId}")
    int updateCompetitionEndTime(@Param("competitionId") Long competitionId,
                                 @Param("endTime") LocalDateTime endTime);
    // 保留
    @Select("SELECT * FROM competitions WHERE id=#{id}")
    CompetitionDO findById(Long id);

    // 获取对应比赛的数据集与标签路径
    @Select("SELECT path FROM competitions WHERE id=#{competitionId}")
    String selectPath(Long competitionId);

//         6. 更新比赛人数
    @Update("UPDATE competition " +
            "SET participant_count = participant_count + 1 " +
            "WHERE competition_id = #{competitionId} " +
            "RETURNING participant_count")
    int incrementParticipantCount(@Param("competitionId") Long competitionId);
}