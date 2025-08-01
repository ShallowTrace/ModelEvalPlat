package com.ecode.modelevalplat.common;


import com.ecode.modelevalplat.common.enums.StatusEnum;

import lombok.Data;

import java.io.Serializable;


@Data
public class ResVo<T> implements Serializable {
    private static final long serialVersionUID = -510306209659393854L;
    // 返回结果说明
    private Status status;

    // 返回的实体结果
    private T result;


    public ResVo() {
    }

    public ResVo(Status status) {
        this.status = status;
    }

    public ResVo(T t) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.result = t;
    }

    public static <T> ResVo<T> ok(T t) {
        return new ResVo<>(t);
    }

    private static final String OK_DEFAULT_MESSAGE = "ok";

    public static ResVo<String> ok() {
        return ok(OK_DEFAULT_MESSAGE);
    }

    public static <T> ResVo<T> fail(StatusEnum status, Object... args) {
        return new ResVo<>(Status.newStatus(status, args));
    }

    public static <T> ResVo<T> fail(Status status) {
        return new ResVo<>(status);
    }
}
