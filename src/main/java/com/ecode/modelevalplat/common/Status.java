package com.ecode.modelevalplat.common;


//import com.ecode.modelevalplat.common.enums.StatusEnum;

import com.ecode.modelevalplat.common.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    /**1
     * 业务状态码
     */
    // 状态码, 0表示成功返回，其他异常返回
    private int code;

    /**
     * 描述信息
     */
    // 正确返回时为ok，异常时为描述文案
    private String msg;

    public static Status newStatus(int code, String msg) {
        return new Status(code, msg);
    }

    public static Status newStatus(StatusEnum status, Object... msgs) {
        String msg;
        if (msgs.length > 0) {
            msg = String.format(status.getMsg(), msgs);
        } else {
            msg = status.getMsg();
        }
        return newStatus(status.getCode(), msg);
    }
}
