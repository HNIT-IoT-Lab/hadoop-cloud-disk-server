package cn.hnit.entity.common;

import java.time.format.DateTimeFormatter;

public class DataFormats {

    public static final String VO_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间转换格式，用于将一个"2022-02-09T00:25:57.724"类型的字符串转成LocalDatetime对象
     */
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]");

}
