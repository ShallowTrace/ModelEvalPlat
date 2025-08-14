package com.ecode.modelevalplat.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserFieldUpdateDTO {

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "ID不能为空")
    private Long id;

    @ApiModelProperty(value = "更新字段名，限定为 username,password,gender,department", required = true)
    @NotNull(message = "字段名不能为空")
    private String fieldName;

    @ApiModelProperty(value = "字段值", required = true)
    @NotNull(message = "字段值不能为空")
    private String fieldValue;

    @ApiModelProperty(value = "原密码，仅修改密码时需要")
    private String oldPassword;
}