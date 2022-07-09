package cn.hnit.Template;


import cn.hnit.entity.Do.FileStatusDo;
import cn.hnit.exception.FileNotFoundException;
import cn.hnit.operate.RedissionBloomFilter;
import cn.hnit.utils.HdfsUtil;
import cn.hnit.utils.IOUtil;
import cn.hnit.utils.RegularUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * HDFS操作类
 *
 * @since: 2022/6/10 9:12
 * @author: 梁峰源
 */
@Slf4j
@Component
//@ConditionalOnBean(FileSystem.class)
public class HadoopTemplate {

    @Autowired
    private FileSystem fileSystem;


    public void uploadLocalFile(String srcFile, String destPath) {
        copyFileToHDFS(false, true, srcFile, destPath);
    }

    public void uploadLocalFile(boolean del, String srcFile, String destPath) {
        copyFileToHDFS(del, true, srcFile, destPath);
    }

    public void delDir(String path) {
        rmdir(path, null);
    }

    public void delFile(String path, String fileName) {
        rmdir(path, fileName);
    }

    public void download(String fileName, String savePath) {
        getFile(fileName, savePath);
    }


    /**
     * 创建目录
     *
     * @param filePath
     * @param create
     * @return
     */
    public boolean existDir(String filePath, boolean create) throws IOException {
        boolean flag = false;
        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath不能为空");
        }
        Path path = new Path(filePath);
        if (create) {
            if (!fileSystem.exists(path)) {
                fileSystem.mkdirs(path);
            }
        }
        if (fileSystem.isDirectory(path)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 文件是否存在
     *
     * @param filePath
     * @return
     */
    public boolean existFile(String filePath) throws IOException {
        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath不能为空");
        }
        Path path = new Path(filePath);
        return fileSystem.exists(path);
    }


    /**
     * 文件上传至 HDFS
     *
     * @param delSrc    指是否删除源文件，true 为删除，默认为 false
     * @param overwrite
     * @param srcFile   源文件，上传文件路径
     * @param destPath  hdfs的目的路径
     */
    public void copyFileToHDFS(boolean delSrc, boolean overwrite, String srcFile, String destPath) {
        // 源文件路径是Linux下的路径，如果在 windows 下测试，需要改写为Windows下的路径，比如D://hadoop/djt/weibo.txt
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path(destPath);
        // 实现文件上传
        try {
            // 获取FileSystem对象
            fileSystem.copyFromLocalFile(delSrc, overwrite, srcPath, dstPath);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * 删除文件或者文件目录
     *
     * @param path
     */
    public void rmdir(String path, String fileName) {
        try {
            if (StringUtils.isNotBlank(fileName)) {
                path = path + "/" + fileName;
            }
            // 删除文件或者文件目录  delete(Path f) 此方法已经弃用
            fileSystem.delete(new Path(path), true);
        } catch (IllegalArgumentException | IOException e) {
            log.error("", e);
        }
    }

    /**
     * @param storePath 文件或目录的路径
     */
    public void rmdir(String storePath) {
        try {
            if (!existFile(storePath)) {
                throw new RuntimeException("文件或文件夹不存在");
            }
            // 删除文件或者文件目录  delete(Path f) 此方法已经弃用
            fileSystem.delete(new Path(storePath), true);
        } catch (IllegalArgumentException | IOException e) {
            log.error("{}", e.getMessage());
        }
    }

    /**
     * 从 HDFS 下载文件
     *
     * @param hdfsFile
     * @param destPath 文件下载后,存放地址
     */
    public void getFile(String hdfsFile, String destPath) {

        Path hdfsPath = new Path(hdfsFile);
        Path dstPath = new Path(destPath);
        try {
            // 下载hdfs上的文件
            fileSystem.copyToLocalFile(hdfsPath, dstPath);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public void writer(String destPath, InputStream in) {
        try {
            FSDataOutputStream out = fileSystem.create(new Path(destPath));
            IOUtils.copyBytes(in, out, fileSystem.getConf());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拿到指定路径下的所有文件名
     *
     * @param filePath
     * @return
     */
    public List<FileStatusDo> listStatus(String filePath) throws IOException {
        if (ObjectUtil.isEmpty(filePath)) {
            return null;
        }
        Path path = new Path(filePath);
        if (!existFile(filePath)) {
            return null;
        }
        //拿到路径下的文件信息
        FileStatus[] fileStatuses = fileSystem.listStatus(path);
        if (ObjectUtil.isEmpty(fileStatuses)) {
            log.info("{}路径下没有文件", filePath);
            return null;
        }
        List<FileStatusDo> list = new ArrayList<>();
        for (FileStatus fileStatus : fileStatuses) {
            list.add(getFileStatus(filePath, fileStatus));
        }
        return list;
    }

    /**
     * 拿到文件的状态
     *
     * @param fileStatus
     * @return
     */
    public FileStatusDo getFileStatus(String filePath, FileStatus fileStatus) {
        if (ObjectUtil.isEmpty(fileStatus)) {
            throw new FileNotFoundException();
        }
        String relativePath;
        Path path = fileStatus.getPath();
        /*
         * 根目录下的文件直接拼接文件名
         * 非根目录需要 xxx + / + xxx
         */
        if ("/".equals(filePath)) {
            relativePath = filePath + path.getName();
        } else {
            relativePath = filePath + "/" + path.getName();
        }
        return new FileStatusDo()
                .setFileName(path.getName())
                .setFilePath(path.toUri().toString())
                .setRelativePath(relativePath)
                .setParentPath(path.getParent().toUri().toString())
                .setOwner(fileStatus.getOwner())
                .setGroup(fileStatus.getOwner())
                .setIsFile(fileStatus.isFile())
                .setDuplicates(String.valueOf(fileStatus.getReplication()))
                .setSize(HdfsUtil.formatFileSize(fileStatus.getLen()))
                .setRights(fileStatus.getPermission().toString())
                .setModifyTime(HdfsUtil.formatTime(fileStatus.getModificationTime()));
    }

    /**
     * 创建文件夹
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public void mkdir(String filePath) throws IOException {
        if (!RegularUtil.checkHdfsFilePath(filePath)) {
            throw new RuntimeException("文件路径格式不正确");
        }
        Path path = new Path(filePath);
        if (fileSystem.exists(path)) {
            throw new RuntimeException("文件已经存在");
        }
        fileSystem.mkdirs(path);
    }


    /**
     * 上传文件至OSS
     *
     * @param storePath 上传的路径
     * @param file      文件对象（File或者MultipartFile类型）
     */
    public boolean uploadFile(String storePath, Object file) throws IOException {
        //如果文件夹不存在就创建
        InputStream is = null;
        // 创建上传Object的Metadata
        FSDataOutputStream fds = fileSystem.create(new Path(storePath));
        try {
            // 文件输入流
            is = IOUtil.getInputStream(file);

            // 文件大小
            Long fileSize = IOUtil.getFileSize(file);
            // 缓存区
            byte[] bt = new byte[2048];
            // 往hdfs中存入数据
            int len = 0;
            while ((len = is.read(bt)) != -1) {
                fds.write(bt, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件异常." + e.getMessage(), e);
        } finally {
            if (ObjectUtil.isNotNull(is)) {
                try {
                    assert is != null;
                    is.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (ObjectUtil.isNotNull(fds)) {
                try {
                    fds.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        return true;
    }


    /**
     * 下载文件
     *
     * @param hdfsUrl   文件在hdfs中的路径
     * @param localPath 文件要保存的本地路径
     */
    public boolean downloadFileToLocal(String hdfsUrl, String localPath) {
        try (
                FSDataInputStream hdr = fileSystem.open(new Path(hdfsUrl));  //从hdfs读出文件流
                FileOutputStream out = new FileOutputStream(localPath);  //写入本地文件
        ) {
            byte[] bytes = new byte[2048];
            //首次读
            int count = hdr.read(bytes, 0, 2048);
            while (count > 0) {
                out.write(bytes, 0, count);
                count = hdr.read(bytes, 0, 2048);
            }
        } catch (IOException e) {
            log.error("下载文件异常." + e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 下载文件
     *
     * @param hdfsUrl 文件在hdfs中的路径
     */
    public FSDataInputStream downloadFile(String hdfsUrl, String fileName) {
        try {
            if (!existFile(hdfsUrl)) {
                throw new RuntimeException("文件不存在");
            }
            return fileSystem.open(new Path(hdfsUrl));  //从hdfs读出文件流
        } catch (IOException e) {
            log.error("下载文件异常." + e.getMessage(), e);
            throw new RuntimeException("下载失败," + e.getMessage());
        }
    }

    /**
     * 这里使用spring自带对象返回，这种方式是将流加载到内存中，不能下载大文件
     *
     * @param in       文件输入流
     * @param fileName 文件名字
     * @return {@link ResponseEntity}
     */
    public ResponseEntity<InputStreamResource> downloadFile(InputStream in, String fileName) {
        try {
            byte[] testBytes = new byte[in.available()];
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");
            headers.add("Content-Disposition", String.format("attachment;filename=\"%s\"", fileName));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "8");
            headers.add("Content-Language", "UTF-8");
            return ResponseEntity.ok().headers(headers).contentLength(testBytes.length)
                    .contentType(MediaType.parseMediaType("application/octet-stream")).body(new InputStreamResource(in));
        } catch (Exception e) {
            throw new RuntimeException("文件下载出错", e);
        }
    }
}
