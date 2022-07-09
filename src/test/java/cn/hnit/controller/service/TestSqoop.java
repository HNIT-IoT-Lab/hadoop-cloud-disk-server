package cn.hnit.controller.service;

import cn.hnit.utils.AESUtil;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/6/28 15:39
 * @author: 梁峰源
 */
public class TestSqoop {
    public static void main(String[] args) throws Exception {
        String xxx = AESUtil.aesEncrypt("admin");
        System.out.println(xxx);
        System.out.println(AESUtil.aesDecrypt("w+NE4VVl702a6mE4n57ZsA=="));
    }
}
