package cn.hnit.exception;

/**
 * @Description:
 * @since: 2022/6/10 9:40
 * @author: 梁峰源
 */
public class FileNotFoundException extends RuntimeException{
    public FileNotFoundException() {
        super("文件不存在!");
    }
    public FileNotFoundException(String message) {
        super(message);
    }
}
