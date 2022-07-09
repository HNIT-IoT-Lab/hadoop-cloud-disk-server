package cn.hnit.core;

import cn.hnit.entity.User;
import cn.hnit.utils.HdfsUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * <p>
 * node结点制造工厂
 * </p>
 *
 * @since: 2022/7/4 13:49
 * @author: 梁峰源
 */
public class FileTreeNodeBuilder {
    // FileTreeNode的构造器
    private static Constructor<?> constructor;

    /**
     * @param user        用户 需要手动拼接用户的前缀 由用户权限和用户名组成 ex: /ROLE_ADMIN/userName
     * @param currentPath
     */
    public static FileTreeNode newTreeNode(User user, String currentPath) {
        // 先判断当前用户有无目录树
        FileTree baseTree = user.getFileTree();
        // 截取 "/" 计算权重
        String[] strs = currentPath.split("/");
        int currentPathWeight = strs.length - 2;
        FileTreeNode fileTreeNode; // 用来表示最终创建的结点
        if (baseTree == null) {
            // 表示用户第一次创建文件或目录
            // 创建根文档树
            baseTree = new FileTree();
            fileTreeNode = newTreeNode(user, baseTree, null, currentPath);
            // 给用户设置baseTree
            user.setFileTree(baseTree);
        } else {
            // 拿到上一个结点
            FileTreeNode preNode = getPreFileTreeNodeByPath(baseTree, currentPath);
            // 创建结点
            // 拿到用户文档树的总深度
            int userFileTreeTotalWeight = getUserFileTreeTotalWeight(baseTree);
            // 如果在当前文档树，即与上一个结点同级
            if(currentPathWeight == userFileTreeTotalWeight){
                // 将当前结点添加到上一个结点同级的文档树中
                // 拿到指定深度的文档树
                FileTree fileTreeByWeight = getFileTreeByWeight(baseTree, userFileTreeTotalWeight);
                fileTreeNode = newTreeNode(user, fileTreeByWeight, preNode, currentPath);
            }else if(currentPathWeight - userFileTreeTotalWeight == 1 ){
                // 在上一个结点的下一级创建目录或文档
                // 先创建该层的文档树
                FileTree fileTree = new FileTree();
                // 设置上一级文档树的下级文档树
                assert preNode != null;
                preNode.getFileTree().setNextFileTree(fileTree);
                // 创建结点
                fileTreeNode = newTreeNode(user, fileTree, preNode, currentPath);
            }else {
                throw new RuntimeException("结点数据不合法");
            }
        }
        return fileTreeNode;
    }

    /**
     * 获得文档树的总深度
     */
    public static int getUserFileTreeTotalWeight(FileTree baseTree) {
        int weight = -1;
        while (baseTree != null) {
            baseTree = baseTree.getNextFileTree();
            weight++;
        }
        return weight;
    }

    /**
     * 获得指定深度的目录树
     */
    public static FileTree getFileTreeByWeight(FileTree fileTree, int weight) {
        for (int i = 0; i < weight && fileTree.getNextFileTree() != null; i++) {
            fileTree = fileTree.getNextFileTree();
        }
        return fileTree;
    }

    /**
     * 获得当前路径下的文件
     */
    private static String getFileNameByPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static String getPreFileNameByPath(String path) {
        String[] split = path.split("/");
        return split[split.length - 2];
    }

    /**
     * 拿到指定路径的上一个结点
     *
     * @param baseTree 用户的根文档树
     * @param path     路径
     */
    private static FileTreeNode getPreFileTreeNodeByPath(FileTree baseTree, String path) {
        // 先切分并拿到路径
        String[] split = path.split("/");
        int weight = split.length - 2;
        // 拿到指定权重的文档树
        FileTree fileTreeByWeight = getFileTreeByWeight(baseTree, weight - 1);
        //取出path的filename
        String filename = getPreFileNameByPath(path);
        // 遍历当前文档树下所有的结点，有返回，没有抛异常
        for (FileTreeNode node : fileTreeByWeight.getNodeList()) {
            if (filename.equals(node.getFileName())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 根据名字获取指定深度
     *
     * @param fileTree 文件要查询的文档树
     * @param fileName 文件名
     */
    private static FileTreeNode getPreFileNodeByFileName(FileTree fileTree, String fileName) {
        for (FileTreeNode node : fileTree.getNodeList()) {
            if (node.getFileName().equals(fileName)) {
                return node;
            }
        }

        return null;
    }

    /**
     * 为用户生成具体的文档树，用以生成当前目录的下一级目录
     *
     * @param user       用户
     * @param fileTree   当前结点的文档树
     * @param parentNode 父结点
     * @param path       文件或文件夹路径
     */
    public static FileTreeNode newTreeNode(User user, FileTree fileTree, FileTreeNode parentNode, String path) {
        // 初始化一个结点
        FileTreeNode node = initTreeNode();
        // 添加当前结点的信息
        node.setPrefix("/" + user.getRole() + "/" + user.getUname());
        // 设置当前结点的路径
        node.setCurrentPath(path);
        // 设置当前结点的深度
        node.setWeight(parentNode == null ? 0 : parentNode.getWeight() + 1);
        // 设置文件在hdfs中的路径
        node.setHDFSPath(node.getPrefix() + node.getCurrentPath());
        // 判断是文件还是目录
        node.setState(HdfsUtil.isDirectory(path) ? FileTreeNode.Type.DIRECTORY : FileTreeNode.Type.FILE);
        // 手动计算当前文件或目录的名字
        node.setFileName(path.substring(path.lastIndexOf("/") + 1));
        // 设置父结点
        node.setParentNode(parentNode);
        // 设置文档树
        node.setFileTree(fileTree);
        // 将当前结点添加到文档树中
        fileTree.add(node);
        return node;
    }

    /**
     * 初始化一个结点
     */
    private static FileTreeNode initTreeNode() {
        FileTreeNode fileTreeNode = null;
        try {
            // 拿到FileTreeNode的构造器
            Constructor<?> constructor = getConstructor();
            fileTreeNode = (FileTreeNode) constructor.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return fileTreeNode;
    }

    private static Constructor<?> getConstructor() throws ClassNotFoundException {
        if (FileTreeNodeBuilder.constructor != null) {
            return FileTreeNodeBuilder.constructor;
        }
        Class<?> clazz = Class.forName("cn.hnit.core.FileTreeNode");
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        declaredConstructors[0].setAccessible(true);
        return declaredConstructors[0];
    }


    public static int getWeight(FileTreeNode node) {
        FileTreeNode parentNode = node.getParentNode();
        int weight = 0;
        if (parentNode != null) {
            weight = parentNode.getWeight() + 1;
        }
        return weight;
    }

    /**
     * 由路径权重获得路径  例如："/usr/local/xxx.mp4"  权重1则返回 /usr/local
     */
    private static String getPathByWeight(String[] strs, int weight) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weight + 1; i++) {
            // 从第一个元素开始拼接，第0个是空串
            sb.append("/").append(strs[i + 1]);
        }
        return sb.toString();
    }
}
