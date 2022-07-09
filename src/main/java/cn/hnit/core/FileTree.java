package cn.hnit.core;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 文档树<br/>
 * 这里需要规定一下一条记录的文档树结构
 * -----------------------------------------------------------------------------------------------------------------
 * 文件id   |    文件路径  |  文件父路径   |    文件对应在Hadoop中的路径    |     文件深度(权重)   |    拓展(子目录)           |
 * 1       /dir1/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2       1(从0开始)     map<String,Tree<String>> |
 * 2       /dir2/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2       1(从0开始)     map<String,Tree<String>> |
 * 3       /dir3/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2       1(从0开始)     map<String,Tree<String>> |
 * 4       /dir4/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2       1(从0开始)     map<String,Tree<String>> |
 * 5       /dir5/dir2      /dir1      /ROLE_ADMIN/userName/dir1/dir2       1(从0开始)     map<String,Tree<String>> |
 * -----------------------------------------------------------------------------------------------------------------
 *
 * @since: 2022/7/4 8:04
 * @author: 梁峰源
 */
@Data
public class FileTree implements Serializable {
    private static final long serialVersionUID = 7419721361122211L;
    private List<FileTreeNode> nodeList = new ArrayList<>();
    private int weight;// 当前路径的深度 根路径为0
    private FileTree nextFileTree;// 下一级目录

    public boolean add(FileTreeNode fileTreeNode) {
        this.weight = fileTreeNode.getWeight();
        return nodeList.add(fileTreeNode);
    }

    /**
     * 获得当前路径下的文件
     */
    public FileTree getFileTree() {
        return this;
    }

    /**
     * 只拿到当前目录下的文件
     */
    public String getFileTreePath() {
        StringBuilder sb = new StringBuilder();
        for (FileTreeNode node : nodeList) {
            sb.append(node.toString()).append("\n");
        }
        return sb.toString();
    }

    public String getFileTreeHdfsPath() {
        StringBuilder sb = new StringBuilder();
        for (FileTreeNode node : nodeList) {
            sb.append(node.getHdfsPath()).append("\n");
        }
        return sb.toString();
    }

    public String showTree() {
        StringBuilder sb = new StringBuilder();
        // 拼接每一级目录的目录树
        FileTree baseTree = this;
        while (baseTree != null) {
            for (FileTreeNode node : baseTree.getNodeList()) {
                sb.append(genDirTree(node));
            }
            baseTree = baseTree.getNextFileTree();
        }
        return sb.toString();
    }

    private String genDirTree(FileTreeNode node) {
        return levelSign(node.getWeight()) + node.getFileName() + "\n";
    }

    //文件层级信息
    private String levelSign(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ├─");
        for (int x = 0; x < level; x++) {
            sb.insert(0, " │   ");
        }
        return sb.toString();
    }
    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileTree fileTree = (FileTree) o;
        return weight == fileTree.weight &&
                Objects.equals(nextFileTree, fileTree.nextFileTree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, nextFileTree);
    }
}
