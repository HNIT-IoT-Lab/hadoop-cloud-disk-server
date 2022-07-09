package cn.hnit.entity.Do;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 描述HDFS中的文件状态
 *
 * @since: 2022/6/10 9:54
 * @author: 梁峰源
 */
@Data
@Accessors(chain = true)
public class FileStatusDo {
    /**
     * 文件名字
     */
    private String fileName;
    /**
     * 文件路径例如 /dir1
     */
    private String filePath;
    /**
     * 文件相对路径 和文件路径一样 /dir1
     */
    private String relativePath;
    /**
     * 该文件的父路径
     */
    private String parentPath;
    /**
     * 创建文件的用户
     */
    private String owner;
    private String group;
    private boolean isFile;
    /**
     * hdfs采取副部备份机制，这里看是不是副本
     */
    private String duplicates;
    private String size;
    private String rights;
    private String modifyTime;

    public FileStatusDo setIsFile(boolean b) {
        isFile = b;
        return this;
    }
}
