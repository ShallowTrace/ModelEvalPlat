package com.ecode.modelevalplat.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserQueryDTO {
    @NotNull(message = "ID不能为空")
    private String id;
}
