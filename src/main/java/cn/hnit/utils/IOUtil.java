package cn.hnit.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/6/27 19:39
 * @author: 梁峰源
 */
public class IOUtil {

    public static InputStream getInputStream(Object file) throws IOException {
        InputStream inputStream = null;
        if (file instanceof File) {
            inputStream = new FileInputStream((File) file);
        } else if (file instanceof MultipartFile) {
            inputStream = ((MultipartFile) file).getInputStream();
        } else {
            throw new RuntimeException("参数必须为 File 或 MultipartFile 类型");
        }
        return inputStream;
    }

    public static Long getFileSize(Object file) {
        Long size = null;
        if (file instanceof File) {
            size = ((File) file).length();
        } else if (file instanceof MultipartFile) {
            size = ((MultipartFile) file).getSize();
        } else {
            throw new RuntimeException("参数必须为 File 或 MultipartFile 类型");
        }
        return size;
    }
}
