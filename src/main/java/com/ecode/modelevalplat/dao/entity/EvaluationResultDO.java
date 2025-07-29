package com.ecode.modelevalplat.dao.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("evaluation_results")
public class EvaluationResultDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long competitionId;
    private Date submitTime;
    private String resultJson;
    private Float score;
}