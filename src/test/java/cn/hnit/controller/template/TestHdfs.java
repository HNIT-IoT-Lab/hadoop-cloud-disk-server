package cn.hnit.controller.template;

import cn.hnit.utils.HdfsUtil;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * @Description:
 * @since: 2022/6/14 15:39
 * @author: 梁峰源
 */

public class TestHdfs {
    @Test
    public void test01() {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(HdfsUtil.formatFileSize(Long.MAX_VALUE >> 23));
    }

    @Test
    public void test02() {
        String str = "";
        Assert.hasText(str, "文件路径不能为空");
        Assert.notNull(str,"不能为空");
    }
}
