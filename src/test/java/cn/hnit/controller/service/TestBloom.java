package cn.hnit.controller.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/6/27 19:00
 * @author: 梁峰源
 */
@SpringBootTest
public class TestBloom {
    @Resource(name = "redissonClient")
    private RedissonClient redissonClient;
    @Test
    void test01() {

    }
}
