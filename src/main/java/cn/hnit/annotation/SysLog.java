package cn.hnit.annotation;

import java.lang.annotation.*;

/**
 * @Description: 自定义日志注解，在需要记录日志的接口上添加此注解
 * @date: 2022/4/2 20:32
 * @author: 梁峰源
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {
    String value() default "";
}
