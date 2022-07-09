package cn.hnit.entity.common;


import org.springframework.http.HttpStatus;

/**
 * 响应结果生成工具
 *
 *
 * @author 梁峰源
 */
public class ResultGenerator {

    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";

    /**
     * 只返回状态
     * @return
     */
    public static Result getSuccessResult() {
        return new Result()
                .setCode(HttpStatus.OK.value())
                .setMessage(DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 失败
     * @param message
     * @return
     */
    public static Result getFailResult(String message) {
        return new Result()
                .setCode(400)
                .setMessage(message);
    }

    /**
     * 成功返回数据
     * @param data
     * @return
     */
    public static Result getSuccessResult(Object data) {
        return new Result()
                .setCode(200)
                .setMessage(DEFAULT_SUCCESS_MESSAGE)
                .setData(data);
    }

    /**
     * 成功返回数据
     * @param data
     * @return
     */
    public static Result getSuccessResult(String message,Object data) {
        return new Result()
                .setCode(200)
                .setMessage(message)
                .setData(data);
    }



    /**
     * 失败
     * @param message
     * @param code
     * @return
     */
    public static Result getFailResult(String message, int code) {
        return new Result()
                .setCode(code)
                .setMessage(message);
    }
}