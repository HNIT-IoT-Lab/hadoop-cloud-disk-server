package cn.hnit.controller.service;

import cn.hnit.entity.Do.FileStatusDo;
import cn.hnit.entity.User;
import cn.hutool.json.JSONUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @since: 2022/6/16 20:59
 * @author: 梁峰源
 */
public class TestJson {
    public static void main(String[] args) {
        List<User> users = Arrays.asList(new User().setUname("张三"), new User().setUname("李四"));
        String json = JSONUtil.toJsonStr(users);
        List<User> users1 = JSONUtil.toList(json, User.class);
        System.out.println("===" + users1);
    }
}
