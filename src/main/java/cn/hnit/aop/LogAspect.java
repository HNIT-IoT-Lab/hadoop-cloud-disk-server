package cn.hnit.aop;

import cn.hnit.annotation.SysLog;
import cn.hnit.entity.Logs;
import cn.hnit.entity.User;
import cn.hnit.operate.RedisOperator;
import cn.hnit.service.LogsService;
import cn.hnit.utils.NETUtils;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.json.JSONUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @date: 2022/5/14 12:56
 * @author: 梁峰源
 */
@Aspect
@Component
public class LogAspect {
    @Value("${spring.redis.token-ttl}")
    private Long tokenTTL;
    @Autowired
    private LogsService logsservice;
    @Autowired
    private RedisOperator redisOperator;

    /**
     * 对包含注解的请求进行拦截
     */
    @Pointcut("@annotation(cn.hnit.annotation.SysLog)")
    public void logPointCut() {
    }

    // 增加切点
    // 第一个*任意返回值
    // com.controller.UsersController表示com.controller包下的UsersController类，
    // "execution(* com.controller.*.*(..))"也可以*代替UsersController就表示包下的任意类
    // 第二个*表示任意方法
    // 第三个(..)表示任意参数
    // 这段就表示在拦截com.controller包下的UsersController类中任意返回值，任意参数的所有方法，你也可以具体到某个方法
//    @Pointcut("execution(* cn.hnit.controller.HdfsController.*(..))")
//    // 任意空方法，主要是用来放上面的注解
//    public void AuthPointCut() {}


//    /**
//     * 登录 下线单独记录
//     */
//    @Around("logPointCut()")
//    public Object aroundLog(ProceedingJoinPoint point) throws Throwable {
//        Object proceed = point.proceed();
//        saveLog(point,null);
//        return proceed;
//    }

    /**
     * 后置通知 用于拦截操作，在方法返回后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()")
    public void doAfterReturn(JoinPoint joinPoint) {
        saveLog(joinPoint, null);
    }

    /**
     * 拦截异常操作，有异常时执行
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrow(JoinPoint joinPoint, Exception e) {
        saveLog(joinPoint, e);
    }

    /**
     * 保存日志
     */
    private void saveLog(JoinPoint joinPoint, Exception e) {
        SysLog sysLog = getAnnotationLog(joinPoint);
        if (sysLog == null) return;
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        // 获得ip地址
        String ip = NETUtils.getIpAddr(httpServletRequest);
        // 拿到用户信息
        User userInfo = getUserInfo(joinPoint);
        String userName = userInfo.getUname();
        // 拿到UA，即浏览器信息
        UserAgent userAgent = NETUtils.getUserAgent(httpServletRequest);
        // 操作内容
        String content = sysLog.value();
        // 操作成功与否
        String type = "success";
        if (e != null) type = e.getMessage();
        Logs log = new Logs(userName, LocalDateTime.now(), ip,
                content, type, userAgent.getBrowser().toString(), userAgent.getOs().toString());
        // 保存日志信息 TODO 这里先在redis中缓存，指定时间段刷新到数据库
        logsservice.save(log);
        // 刷新用户token过期时间
        refreshTokenTTL(joinPoint);
    }


    /**
     * 是否存在注解，如果存在就获取
     */
    private SysLog getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(SysLog.class);
        }
        return null;
    }


    /**
     * 获取当前的request
     * 这里如果报空指针异常是因为单独使用spring获取request
     * 需要在配置文件里添加监听
     * <listener>
     * <listener-class>
     * org.springframework.web.context.request.RequestContextListener
     * </listener-class>
     * </listener>
     */
    public HttpServletRequest getHttpServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        return sra.getRequest();
    }

    /**
     * 获取方法参数
     */
    private Map<Object, Object> getServiceMethodParams(JoinPoint joinPoint) {
        // 下面两个数组中，参数值和参数名的个数和位置是一一对应的。
        Object[] args = joinPoint.getArgs(); // 参数值
        Object[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames(); // 参数名
        return parseMap(argNames, args);
    }

    /**
     * 将方法列表转成map
     */
    private Map<Object, Object> parseMap(Object[] argNames, Object[] args) {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < argNames.length; i++) {
            map.put(argNames[i], args[i]);
        }
        return map;
    }

    /**
     * 拿到用户信息
     */
    private User getUserInfo(JoinPoint joinPoint) {
        Map<Object, Object> params = getServiceMethodParams(joinPoint);
        // 拿到token
        String token = (String) params.get("token");
        User user;
        if (token == null) {
            // 如果为空，可能是登录操作，直接注入username
            user = new User().setUname((String) params.get("username"));
        } else {
            // 拿到用户的信息
            user = JSONUtil.toBean(redisOperator.get(token), User.class);
        }
        return user;
    }

    /**
     * 刷新token的过期时间
     */
    private void refreshTokenTTL(JoinPoint joinPoint) {
        Map<Object, Object> params = getServiceMethodParams(joinPoint);
        // 拿到token
        String token = (String) params.get("token");
        if (token != null) // 刷新token的过期时间
            redisOperator.expire(token, tokenTTL);
    }
}

