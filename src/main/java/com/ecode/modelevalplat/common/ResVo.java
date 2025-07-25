package com.ecode.modelevalplat.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应包装类
 * 统一前后端返回结构，便于前端解析和后端维护
 *
 * @param <T> 实际业务返回数据类型
 */
@Data
public class ResVo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "状态码", required = true)
    private int code;

    @ApiModelProperty(value = "状态说明", required = true)
    private String msg;

    @ApiModelProperty(value = "业务数据", required = true)
    private T data;

    // ---------- 构造器 ----------

    private ResVo(StatusEnum status, T data) {
        this.code = status.getCode();
        this.msg = status.getMsg();
        this.data = data;
    }

    private ResVo(StatusEnum status) {
        this(status, null);
    }

    // ---------- 成功响应 ----------

    /**
     * 成功响应，带格式化消息（带数据）
     */
    public static <T> ResVo<T> ok(StatusEnum status, T data, Object... args) {
        String formattedMsg = String.format(status.getMsg(), args);
        ResVo<T> vo = new ResVo<>(status, data);
        vo.setMsg(formattedMsg);
        return vo;
    }

    /**
     * 成功响应，格式化消息（无数据）
     */
    public static ResVo<String> ok(StatusEnum status, Object... args) {
        String formattedMsg = String.format(status.getMsg(), args);
        ResVo<String> vo = new ResVo<>(status, "ok");
        vo.setMsg(formattedMsg);
        return vo;
    }

    // ---------- 失败响应 ----------

    /**
     * 失败响应，无格式化参数
     */
    public static <T> ResVo<T> fail(StatusEnum status) {
        return new ResVo<>(status);
    }

    /**
     * 失败响应，带格式化参数
     */
    public static <T> ResVo<T> fail(StatusEnum status, Object... args) {
        String formattedMsg = String.format(status.getMsg(), args);
        ResVo<T> vo = new ResVo<>(status);
        vo.setMsg(formattedMsg);
        return vo;
    }

    // ---------- 私有方法 ----------
    private void setMsg(String msg) {
        this.msg = msg;
    }
}
