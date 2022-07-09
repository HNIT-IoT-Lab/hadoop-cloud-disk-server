package cn.hnit.controller;

import cn.hnit.annotation.SysLog;
import cn.hnit.entity.User;
import cn.hnit.entity.UserLogAnalyse;
import cn.hnit.entity.Vo.UserLogAnalyseVo;
import cn.hnit.entity.common.Result;
import cn.hnit.entity.common.ResultGenerator;
import cn.hnit.mapper.UAMapper;
import cn.hnit.operate.RedisOperator;
import cn.hnit.service.UAService;
import cn.hnit.service.UserService;
import cn.hnit.utils.AESUtil;
import cn.hnit.utils.JwtUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 梁峰源
 * @since 2022-06-08
 */
@Api(tags = "用户模块")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Value("${spring.redis.token-ttl}")
    private Long tokenTTL;
    @Autowired
    private UserService userService;
    @Autowired
    private UAMapper uaMapper;
    @Autowired
    private RedisOperator redisOperator;

    @SysLog("用户注册")
    @PostMapping("/register")
    @ApiOperation("用户登录接口")
    public Result register(String username, String password) {
        // 先判断用户名是否已经存在
        User user = userService.getOne(new QueryWrapper<User>().eq("uname", username));
        if (user != null) return ResultGenerator.getFailResult("用户名已存在");
        user = new User()
                .setRole(User.role.ROLE_USER)
                .setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .setUname(username)
                .setUpwd(AESUtil.aesEncrypt(password));
        // 存储到数据库中
        userService.save(user);
        return ResultGenerator.getSuccessResult();
    }

    @SysLog("登录操作")
    @PostMapping("/login")
    @ApiOperation("用户登录接口")
    public Result login(String username, String password) {
        if (ObjectUtil.isEmpty(username) || ObjectUtil.isEmpty(password)) {
            return ResultGenerator.getFailResult("用户名或账号为空");
        }
        User user = userService.getOne(new QueryWrapper<User>().eq("uname", username));
        if (ObjectUtil.isEmpty(user)) {
            return ResultGenerator.getFailResult("用户名错误");
        }
        //解密密码
        String pwd = AESUtil.aesDecrypt(user.getUpwd());
        if (password.equals(pwd)) {
            //使用用户的用户名和密码加密作为token，使用jwt进行鉴权
//            String token = JwtUtil.sign(user.getUname(), "-1");
            // 使用hutool生成uuid作为token
            String token = IdUtil.simpleUUID();
            // 将用户信息保存到redis中
            redisOperator.set(token, JSONUtil.toJsonStr(user), tokenTTL);
            HashMap<String, String> map = new HashMap<>();
            map.put("token", token);
            return ResultGenerator.getSuccessResult(map);
        }
        return ResultGenerator.getFailResult("密码错误");
    }

    @SysLog("获取用户信息")
    @GetMapping("/getInfo")
    @ApiOperation("获取用户信息接口")
    public Result info(@RequestParam("token") String token) {
        Assert.hasText(token, "用户未登录");
        // 验证token的合法和有效性
//        String tokenValue = JwtUtil.verity(token);// success:zhangsan1
        String userInfo = redisOperator.get(token);
        if (!ObjectUtil.isEmpty(userInfo)) {
            // zhangsan1
//            String username = tokenValue.replaceFirst(JwtUtil.TOKEN_SUCCESS, "");
//            // 根据用户名达到用户的信息
//            User user = userService.getOne(new QueryWrapper<User>().eq("uname", username));
            // 拿到用户的信息
            User user = JSONUtil.toBean(userInfo, User.class);
            // 这里的权限需要设置为非空数组，和前端对应
            //给用户设定角色组，一个用户可以拥有多个角色
            List<String> roles = Collections.singletonList(user.getRole());
            user.setRoles(roles);
            // 返回用户信息
            return ResultGenerator.getSuccessResult(user);
        } else {
            return ResultGenerator.getFailResult("token有误");
        }
    }

    @SysLog("用户下线")
    @PostMapping("/logout")
    @ApiOperation("用户下线接口")
    public Result logout(@RequestHeader("token") String token) {
        Assert.hasText(token, "用户未登录");
        // 验证token的合法和有效性
//        String tokenValue = JwtUtil.verity(token);// success:zhangsan1
        // 获取token中的用户名
//        String username = tokenValue.replaceFirst(JwtUtil.TOKEN_SUCCESS, "");
        // 移除token
        redisOperator.del(token);
        return new Result()
                .setCode(200)
                .setMessage("logout success")
                .setData("logout success");
    }

    @SysLog("用户查询分析日志")
    @PostMapping("/userLogAnalyse")
    @ApiOperation("用户查询分析日志接口")
    public Result userLogAnalyse(@RequestHeader("token") String token){
        Assert.hasText(token, "用户未登录");
        // 根据token拿到用户的信息
        User user = getUserByToken(token);
        // 查询用户的数据
        List<UserLogAnalyse> userLogAnalyses = uaMapper.listLog(user.getUname());
        // 包装数据并返回给用户
        UserLogAnalyseVo vo = new UserLogAnalyseVo(userLogAnalyses);
        return ResultGenerator.getSuccessResult(vo);
    }


    /**
     * 根据token拿到用户的数据
     */
    private User getUserByToken(String token) {
        // 拿到用户的数据
        String obj = redisOperator.get(token);
        Assert.notNull(obj, "没有用户的信息");
        return JSONUtil.toBean(obj, User.class);
    }
}

