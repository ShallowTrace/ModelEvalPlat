package com.ecode.modelevalplat.dao.mapper;


import com.ecode.modelevalplat.dao.entity.CompetitionDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompetitionMapper {
    @Insert("INSERT INTO competitions(name, description, start_time, end_time, path, " +
            "is_active, participant_count, daily_submission_limit, created_at) " +
            "VALUES(#{name}, #{description}, #{startTime}, #{endTime}, #{path}, " +
            "#{isActive}, #{participantCount}, #{dailySubmissionLimit}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CompetitionDO competition);

    @Update("UPDATE competitions SET name=#{name}, description=#{description}, " +
            "start_time=#{startTime}, end_time=#{endTime}, path=#{path}, " +
            "is_active=#{isActive}, participant_count=#{participantCount}, " +
            "daily_submission_limit=#{dailySubmissionLimit} WHERE id=#{id}")
    void update(CompetitionDO competition);

    @Delete("DELETE FROM competitions WHERE id=#{id}")
    void delete(Long id);

    @Select("SELECT * FROM competitions WHERE id=#{id}")
    CompetitionDO findById(Long id);

    @Select("SELECT * FROM competitions WHERE is_active = 1")
    List<CompetitionDO> findActiveCompetitions();

    @Select("SELECT * FROM competitions")
    List<CompetitionDO> findAll();
}