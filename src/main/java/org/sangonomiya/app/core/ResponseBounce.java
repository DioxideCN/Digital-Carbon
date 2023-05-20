package org.sangonomiya.app.core;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

/**
 * 后端响应体，在前端收到的格式为{"code": int, "message": string, "data": T}
 *
 * @author Dioxide.CN
 * @date 2023/2/28 14:02
 * @since 1.0
 */
@ApiModel(value = "响应结果", description = "公共返回对象")
@ToString
public class ResponseBounce<T> {

    @ApiModelProperty("响应码")
    private int code;
    @ApiModelProperty("附加消息")
    private String msg;
    @ApiModelProperty("返回数据体")
    private T data;

    public ResponseBounce<T> setCode(Code response) {
        this.code = response.code;
        return this;
    }

    public ResponseBounce<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public int getCode() {
        return code;
    }

    public ResponseBounce<T> setMsg(String message) {
        this.msg = message;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseBounce<T> setData(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public enum Code {
        SUCCESS(200),
        FAIL(400),
        UNAUTHORIZED(401),
        NOT_FOUND(404),
        SERVER_ERROR(500);

        final int code;

        Code(int code) {
            this.code = code;
        }
    }
}
