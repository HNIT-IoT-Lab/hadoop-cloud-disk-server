package cn.hnit.controller.core;

import cn.hnit.core.FileTree;
import cn.hnit.core.FileTreeNode;
import cn.hnit.core.FileTreeNodeBuilder;
import cn.hnit.entity.User;
import cn.hnit.utils.HdfsUtil;
import org.junit.Test;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/7/4 9:42
 * @author: 梁峰源
 */
public class TestFileTree {
    @Test
    public void test01() {
        User user = new User().setUname("zhangsan").setRole(User.role.ROLE_USER);
        FileTreeNodeBuilder.newTreeNode(user, "/dir");
        FileTreeNodeBuilder.newTreeNode(user, "/dir2");
        FileTreeNodeBuilder.newTreeNode(user, "/dir3");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/xx.java");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir3");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir4");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir5");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir6");
        FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir3/xx2.java");
        FileTreeNode node = FileTreeNodeBuilder.newTreeNode(user, "/dir/dir2/dir3/xx2.java");
        System.out.println(node.getCurrentPath());
        // 拿到指定用户的文档树
        FileTree fileTree = user.getFileTree();
        System.out.println(fileTree.getFileTreePath());
        System.out.println(fileTree.getNextFileTree().getNextFileTree().getNextFileTree().getFileTreePath());
        System.out.println(fileTree.getFileTreeHdfsPath());
        System.out.println(fileTree.showTree());
    }

    @Test
    public void test02() {
        String str = "/usr/local/xxx.mp4";
        System.out.println(str.split("/").length);
        String[] split = str.split("/");
        System.out.println(split[split.length -2]);
        System.out.println(getCurrentPathByWeight(split, 1));
    }

    private String getCurrentPathByWeight(String[] strs, int weight) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weight + 1; i++) {
            // 从第一个元素开始拼接，第0个是空串
            sb.append("/").append(strs[i + 1]);
        }
        return sb.toString();
    }

    @Test
    public void test03() {
        String path = "/ROLE_USER/zhangsan/dir3";
        String[] split = path.split("/");
        String substring;
        for (int i = 0; i < 3; i++) {
            path= path.substring(path.indexOf("/") + 1);
        }
        System.out.println(path);
    }

    @Test
    public void test04() {
        String str = "/usr/local/xxx.mp4";
        User user = new User().setUname("zhangsan").setRole(User.role.ROLE_USER);
        String path = HdfsUtil.getHdfsPaths(user, str);
        System.out.println(HdfsUtil.getHdfsPaths(user,str   ));
        System.out.println(HdfsUtil.getRelativePath(path));
    }
}
