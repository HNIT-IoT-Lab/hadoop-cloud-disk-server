package cn.hnit.utils;


import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
public class AESUtil {
    private static final String key = "qwertyuiopasdfgh";//密码 随便设置
    private static final String ivKey = "qwertyuiopasdfgh";//偏移量iv 随便设置

    /**
     * 加密
     *
     * @param content
     * @param strKey
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(String content, String strKey) throws Exception {

        SecretKeySpec skeySpec = getKey(strKey);//构建密钥
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //使用CBC模式，需要一个偏移量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return encrypted;
    }

    /**
     * 解密
     *
     * @param strKey
     * @param content
     * @return
     * @throws Exception
     */
    private static String decrypt(byte[] content, String strKey) throws Exception {

        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] original = cipher.doFinal(content);
        return new String(original);
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {

        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）

        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }

        return new SecretKeySpec(arrB, "AES");
    }

    /**
     * base 64 encode
     *
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    private static String base64Encode(byte[] bytes) {

        return new BASE64Encoder().encode(bytes);
    }

    /**
     * base 64 decode
     *
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    private static byte[] base64Decode(String base64Code) throws Exception {

        return new BASE64Decoder().decodeBuffer(base64Code);
    }

    /**
     * AES加密为base 64 code
     *
     * @param content 待加密的内容
     * @return 加密后的base 64 code
     * @throws Exception //加密传String类型，返回String类型
     */
    public static String aesEncrypt(String content) {

        try {
            //此处使用BAES64做转码功能，同时能起到2次加密的作用。
            String xx = base64Encode(encrypt(content, key));
            xx = xx.replace("\r\n", "");//根据具体情况判断，是否需要替换行行
            return xx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }
    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string //解密传String类型，返回String类型
     * @throws Exception
     */

    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr
     * @return
     */
    public static String aesDecrypt(String encryptStr) {

        try {
            return decrypt(base64Decode(encryptStr), key);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("解密失败", e);
        }
        return "fail";
    }
}


