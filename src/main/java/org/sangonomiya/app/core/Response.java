package org.sangonomiya.app.core;

import io.swagger.annotations.ApiModel;
import org.sangonomiya.app.core.ResponseBounce;

/**
 * 对返回的结果进行封装
 * @author Dioxide.CN
 * @date 2023/2/28 14:08
 * @since 1.0
 */
public class Response {

    private final static String SUCCESS = "SUCCESS";

    /**
     * 请求成功且不带任何返回参数
     * @return 返回一个封装的请求成功的结果
     */
    public static <T> ResponseBounce<T> success() {
        return new ResponseBounce<T>()
                .setCode(ResponseBounce.Code.SUCCESS)
                .setMsg(SUCCESS);
    }

    /**
     * 请求成功且带返回参数data
     * @param data 返回给前端的参数
     * @return 返回一个封装的请求成功的结果
     */
    public static <T> ResponseBounce<T> success(T data) {
        return new ResponseBounce<T>()
                .setCode(ResponseBounce.Code.SUCCESS)
                .setMsg(SUCCESS)
                .setData(data);
    }

    /**
     * 请求失败且带一个失败的消息
     * @param message 请求失败的消息
     * @return 返回一个封装的请求失败的结果
     */
    public static <T> ResponseBounce<T> fail(String message) {
        return new ResponseBounce<T>()
                .setCode(ResponseBounce.Code.FAIL)
                .setMsg(message);
    }

    /**
     * 返回一个不带返回参数的自定义请求结果
     * @param code 请求的code
     * @param message 请求返回的消息
     * @return 返回一个封装的请求结果
     */
    public static <T> ResponseBounce<T> info(int code, String message) {
        return new ResponseBounce<T>()
                .setCode(code)
                .setMsg(message);
    }

    /**
     * 返回一个自定义请求结果
     * @param code 请求的code
     * @param message 请求返回的消息
     * @return 返回一个封装的请求结果
     */
    public static <T> ResponseBounce<T> info(int code, String message, T data) {
        return new ResponseBounce<T>()
                .setCode(code)
                .setMsg(message)
                .setData(data);
    }

}
