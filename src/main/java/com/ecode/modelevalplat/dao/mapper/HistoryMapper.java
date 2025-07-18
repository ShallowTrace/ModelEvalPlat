package com.ecode.modelevalplat.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecode.modelevalplat.dto.HistoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HistoryMapper extends BaseMapper<HistoryDTO> {
    //提交时间排序
    @Select("SELECT * FROM history WHERE user_id = #{userId} ORDER BY submit_time DESC")
    Page<HistoryDTO> selectHistoryRecords(Page<HistoryDTO> page, Long userId);

    //根据竞赛id查询用户历史记录
    @Select("SELECT * FROM history WHERE user_id = #{userId} AND competition_id = #{competitionId} ORDER BY submit_time DESC")
    Page<HistoryDTO> selectHistoryByCompetition(Page<HistoryDTO> page, Long userId, Long competitionId);
}
