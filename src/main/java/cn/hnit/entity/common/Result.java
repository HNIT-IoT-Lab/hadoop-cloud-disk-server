package cn.hnit.entity.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 * @author 梁峰源
 */

@Data
@Accessors(chain = true)
public class Result implements Serializable {
    private int code;
    private String message;
    private Object data;
}