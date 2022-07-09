package cn.hnit.controller.Mapper;

import cn.hnit.mapper.UAMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/7/7 8:13
 * @author: 梁峰源
 */
@SpringBootTest
public class UAmapper {
    @Autowired
    private UAMapper uaMapper;

    @Test
    void testUAMapper() {
        System.out.println(uaMapper.listLog("lfy"));
    }
}
