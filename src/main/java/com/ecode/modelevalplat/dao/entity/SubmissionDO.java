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
@TableName("submissions")
public class SubmissionDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long competitionId;
    private String modelPath;
    private String status; // 实际应为枚举类型
    private Date submitTime;
}