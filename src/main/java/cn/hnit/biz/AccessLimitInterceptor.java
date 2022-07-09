package cn.hnit.biz;

import cn.hnit.annotation.AccessLimit;
import cn.hnit.entity.common.ResultGenerator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author: 梁峰源
 * @date: 2022/4/4 9:43
 * @Description: 自定义拦截器，配合注解AccessLimit对接口的并发量进行限制
 */
@Slf4j
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    //使用RedisTemplate操作redis
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    /**
     * 在业务处理器处理请求之前被调用。预处理，可以进行编码、安全控制、权限校验等处理；
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //不是指定handle，放行
        if (!(handler instanceof HandlerMethod)) return true;
        //限流操作
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method methods = handlerMethod.getMethod();
        //如果拦截的接口上没有AccessLimit限制，放行
        if (!methods.isAnnotationPresent(AccessLimit.class)) {
            return true;
        }
        AccessLimit accessLimit = methods.getAnnotation(AccessLimit.class);
        //如果没有该注解，直接放行
        if (ObjectUtil.isNull(accessLimit)) return true;
        //拿到注解上面标记的属性
        //并发限制数
        int maxLimit = accessLimit.limit();
        //时间
        int sec = accessLimit.sec();
        //时间单位
        TimeUnit timeUnit = accessLimit.TIME_UNIT();
        //将请求的ipAddr和URI作为key存入redis中
        String key = request.getRequestURL().toString();
        //拿到redis中现存的并发数
        Integer limit = redisTemplate.opsForValue().get(key);
        log.debug("当前并发数为：{},key:{}\nsec:{},timeUnit:{}", limit, key, sec, timeUnit);
        if (ObjectUtil.isNull(limit)) {
            //set时一定要加超时时间
            redisTemplate.opsForValue().set(key, 1, sec, timeUnit);
        } else if (limit < maxLimit) {
            //如果接口并发量没有满，运行请求
            redisTemplate.opsForValue().set(key, limit + 1, sec, timeUnit);
        } else {
            output(response,
                    JSONUtil.toJsonStr(ResultGenerator.getFailResult(
                            "正在有其他同学操作", 610)));
            return false;
        }
        return true;
    }

    /**
     * 返回响应
     */
    public void output(HttpServletResponse response, String msg) {
        response.setContentType("application/json;charset=UTF-8");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
