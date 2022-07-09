package cn.hnit.core;

import lombok.Data;

/**
 * <p>
 * 文档树结点
 * 这里需要规定一下一条记录的文档树结构
 * -----------------------------------------------------------------------------------------------------------------
 * 文件id   |    文件路径  |  文件父路径   |    文件对应在Hadoop中的路径    |     文件深度(权重)   |    拓展(子目录)           |
 *   1       /dir1/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2      1(从0开始)     map<String,Tree<String>> |
 *   2       /dir2/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2      1(从0开始)     map<String,Tree<String>> |
 *   3       /dir3/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2      1(从0开始)     map<String,Tree<String>> |
 *   4       /dir4/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2      1(从0开始)     map<String,Tree<String>> |
 *   5       /dir5/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2      1(从0开始)     map<String,Tree<String>> |
 * -----------------------------------------------------------------------------------------------------------------
 *
 * @since: 2022/7/4 8:10
 * @author: 梁峰源
 */
@Data
public class FileTreeNode {
    private FileTree FileTree; // 当前结点中包含的文件和目录
    private FileTreeNode parentNode; // 上一个结点
    private String hdfsPath; // 文件在hdfs中的路径
    private String currentPath;// 当前结点的路径
    private String fileName;// 当前文件或者目录名
    private int state;// 用来表示文件状态 1 是文件 2 是目录 3 是 共享文件
    private String prefix;// hdfs中的前缀
    private int weight = 0;//当前结点的深度，第一级目录深度为0

    static class Type {
        public static final int FILE = 1;
        public static final int DIRECTORY = 2;
        public static final int SHARE_FILE = 3;
    }

    private FileTreeNode() {

    }


    /**
     * 获取当前结点的路径
     */
    public String getPath() {
        return this.currentPath;
    }

    /**
     * 获取父结点的路径
     */
    public String getParentPath() {
        return parentNode.getPath();
    }

    public String getHdfsPath() {
        return this.hdfsPath;
    }

    public FileTreeNode setHDFSPath(String hdfsPath) {
        this.hdfsPath = hdfsPath;
        return this;
    }

    public int getState() {
        return this.state;
    }

    public boolean isShareFile() {
        return this.state == Type.SHARE_FILE;
    }

    public boolean isFile() {
        return this.state == Type.FILE;
    }

    public boolean isDirectory() {
        return this.state == Type.DIRECTORY;
    }

    public int getWeight() {
        return weight;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return currentPath;
    }

    @Override
    public boolean equals(Object o) {
        return this.currentPath.equals(((FileTreeNode) o).getCurrentPath());
    }

    @Override
    public int hashCode() {
        return currentPath.hashCode();
    }
}
