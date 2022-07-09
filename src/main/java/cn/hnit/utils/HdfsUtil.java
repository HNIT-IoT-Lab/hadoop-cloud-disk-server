package cn.hnit.utils;

import cn.hnit.entity.Do.FileStatusDo;
import cn.hnit.entity.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @since: 2022/6/10 9:55
 * @author: 梁峰源
 */
public class HdfsUtil {
    private static final int _B = 1;
    private static final int _KB = 1 << 10;
    private static final int _MB = 1 << 20;
    private static final int _GB = 1 << 30;
    private static final Long _TB = Long.MAX_VALUE >> 23;
    private static final Long _PB = Long.MAX_VALUE >> 13;


    /**
     * 格式化大小
     */
    public static String formatFileSize(Long size) {
        if (size == 0) return "0KB";
        //保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (size < _KB) {
            return decimalFormat.format((double) size) + "B";
        } else if (size < _MB) {
            return decimalFormat.format((double) size / _KB) + "KB";
        } else if (size < _GB) {
            return decimalFormat.format((double) size / _MB) + "MB";
        } else if (size < _TB) {
            return decimalFormat.format((double) size / _GB) + "GB";
        } else if (size < _PB) {
            return decimalFormat.format((double) size / _TB) + "TB";
        } else {
            return "-1";
        }
    }

    /**
     * 格式化时间
     */
    public static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(new Date(time));
    }

    /**
     * 包含 . 表示是文件
     */
    public static long getFileNum(List<FileStatusDo> list) {
        return list.stream()
                .filter(e -> e.getRelativePath().contains("."))
                .count();
    }

    /**
     * 不包含 . 表示是目录
     */
    public static long getListNum(List<FileStatusDo> list) {
        return list.stream()
                .filter(e -> !e.getRelativePath().contains("."))
                .count();
    }

    public static boolean isFile(String path) {
        return path.contains(".");
    }

    public static boolean isDirectory(String path) {
        return !path.contains(".");
    }

    /**
     * 获得用户的真实路径，即将前缀去掉
     */
    public static String getRelativePath(String path) {
        if (path == null || path.isEmpty()) return "";
        for (int i = 0; i < 3; i++) {
            path = path.substring(path.indexOf("/") + 1);
        }
        return "/" + path;
    }

    /**
     * 获得用户在hdfs中的路径，即加上用户的前缀
     */
    public static String getHdfsPaths(User user, String path) {
        return "/" + user.getRole() + "/" + user.getUname() + path;
    }
}
