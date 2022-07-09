package cn.hnit.controller;

import cn.hnit.Template.HadoopTemplate;
import cn.hnit.annotation.SysLog;
import cn.hnit.entity.Do.FileStatusDo;
import cn.hnit.entity.User;
import cn.hnit.entity.Vo.FileListVo;
import cn.hnit.entity.common.Result;
import cn.hnit.entity.common.ResultGenerator;
import cn.hnit.operate.RedisOperator;
import cn.hnit.operate.RedissionBloomFilter;
import cn.hnit.utils.HdfsUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataInputStream;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户文件系统操作
 * </p>
 *
 * @since: 2022/6/9 10:19
 * @author: 梁峰源
 */
@Api(tags = "文件模块")
@Slf4j
@RestController
@RequestMapping("/file")
public class HdfsController {
    @Autowired
    private HadoopTemplate hadoopTemplate;
    @Autowired
    private RedisOperator redisOperator;


    /**
     * 每个用户都拥有一棵自己的文档树<br/>
     * 文档树信息存在redis中，对应hadoop中的路径，仅在hadoop路径前面加上 + "/userFile" + "/" + 用户的名称<br/>
     * 除此之外，每个文件的hash单独存在一张表中，表中记录当前文件存在的用户路径<br/>
     * 如果后面上传时发现该文件已经存在，则将文件移动到共享文件夹之中 ""，并将两个用户的引用和hash表的引用指向共享文件夹
     */
    @SysLog("用户查询网盘信息")
    @GetMapping("/fileList")
    @ApiOperation(value = "目录详情接口", response = FileStatusDo.class)
    public Result fileList(String path,
                           @RequestHeader("token") String token,
                           @RequestParam(defaultValue = "1") int pageNo,
                           @RequestParam(defaultValue = "20") int pageSize) {
        Assert.notNull(token, "token不能为空");
        // 从redis中拿到用户的信息
        User user = getUserByToken(token);
        List<FileStatusDo> fileStatusDos;
        try {
            // 需要转换为用户的真实路径
            fileStatusDos = hadoopTemplate.listStatus(HdfsUtil.getHdfsPaths(user, path));
        } catch (IOException ioException) {
            return ResultGenerator.getFailResult("文件路径错误！");
        }
        if(ObjectUtil.isEmpty(fileStatusDos)) fileStatusDos = new ArrayList<>();
        FileListVo fileListVo = new FileListVo();
        //返回指定的数据，这里也可以在浏览器做缓存
        List<FileStatusDo> collect = fileStatusDos.stream()
                .skip((pageNo - 1) * pageSize)
                .limit(pageSize)
                //这里需要将用户的hdfs路径转换相对路径，即去掉用户的前缀
                .peek(e -> {
                    e.setFilePath(HdfsUtil.getRelativePath(e.getRelativePath()));
                    e.setRelativePath(HdfsUtil.getRelativePath(e.getRelativePath()));
                    e.setParentPath(HdfsUtil.getRelativePath(e.getParentPath()));
                })
                .collect(Collectors.toList());
        //包装一下数据
        fileListVo.setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(fileStatusDos.size())
                .setFileNum(HdfsUtil.getFileNum(collect))
                .setListNum(HdfsUtil.getListNum(collect))
                .setFileStatusDos(collect);
        return ResultGenerator.getSuccessResult(fileListVo);
    }

    @SysLog("用户创建文件夹")
    @GetMapping("/createDir")
    @ApiOperation(value = "建立文件夹")
    public Result createDir(String path, @RequestHeader("token") String token) {
        Assert.notNull(token, "token不能为空");
        Assert.hasText(path, "文件路径不能为空");
        try {
            User user = getUserByToken(token);
            // 需要转换为正式的路径
            hadoopTemplate.mkdir(HdfsUtil.getHdfsPaths(user, path));
        } catch (Exception e) {
            log.error("文件夹创建失败");
            return ResultGenerator.getFailResult("文件夹创建失败，" + e.getMessage());
        }
        return ResultGenerator.getSuccessResult("文件夹创建成功");
    }

    /**
     * 注意删除文件夹会将文件也一起删除，风险很高
     */
    @SysLog("用户删除文件夹")
    @GetMapping("/deleteDir")
    @ApiOperation(value = "删除文件夹")
    public Result deleteDir(String path, @RequestHeader("token") String token) {
        Assert.hasText(path, "文件路径不能为空");
        try {
            User user = getUserByToken(token);
            hadoopTemplate.delDir(HdfsUtil.getHdfsPaths(user, path));
        } catch (Exception e) {
            log.error("文件夹删除失败");
            return ResultGenerator.getFailResult("文件夹名不能为空" + e.getMessage());
        }
        return ResultGenerator.getSuccessResult("文件夹删除成功");
    }

    /**
     * 上传文件
     *
     * @param storePath 文件所在文件夹路径
     * @param file      {@link MultipartFile}
     * @return {@link Result}
     */
    @SysLog("用户上传文件")
    @PostMapping("/uploadFile")
    @ApiOperation(value = "上传文件")
    public Result uploadFile(@ApiParam(value = "文件需要存放的路径", example = "/input/test/xxx.mp4")
                                     String storePath,
                             @RequestPart("file") MultipartFile file,
                             @RequestParam("token") String token) {
        Assert.notNull(token, "token不能为空");
        Assert.hasText(storePath, "文件路径不能为空");
        try {
            // 布隆过滤
            User user = getUserByToken(token);
            hadoopTemplate.uploadFile(HdfsUtil.getHdfsPaths(user, storePath), file);
        } catch (Exception e) {
            log.error("文件上传失败:{}", e.getMessage());
            return ResultGenerator.getFailResult("文件上传失败:" + e.getMessage());
        }
        return ResultGenerator.getSuccessResult("文件上传成功");
    }

    /**
     * 文件下载
     * {@link ResponseEntity}的优先级优于{@link ResponseBody}<br/>
     * 在不是ResponseEntity的情况下才去检查有没有{@link ResponseBody}注解<br/>
     * 如果响应类型是ResponseEntity可以不写@ResponseBody注解，写了也没有关系。简单的说<br/>
     * {@link ResponseBody}可以直接返回Json结果，
     * {@link ResponseEntity}不仅可以返回json结果，还可以定义返回的HttpHeaders和HttpStatus
     *
     * @param relativePath hdfs中文件目录路径
     * @param fileName     文件的名字
     */
    @SysLog("用户下载文件")
    @PostMapping("/downLoadFile")
    @ApiOperation(value = "下载文件")
    public ResponseEntity<StreamingResponseBody> downLoadFile(
            @ApiParam(value = "hdfs中文件目录路径", example = "/input/test")
                    String relativePath,
            @ApiParam(value = "文件的名字", example = "xxx.mp4")
                    String fileName,
            @RequestHeader("token") String token) {
        Assert.notNull(token, "token不能为空");
        Assert.hasText(relativePath, "文件路径不能为空");
        try {
            User user = getUserByToken(token);
            // 拿到输出流
            FSDataInputStream inputStream
                    = hadoopTemplate.downloadFile(HdfsUtil.getHdfsPaths(user, relativePath), fileName);
            // 大文件不能以流的形式传输
            StreamingResponseBody responseBody = outputStream -> {
                int numberOfByBytesToWrite;
                byte[] buffer = new byte[1024];
                while ((numberOfByBytesToWrite = inputStream.read(buffer, 0, buffer.length)) != -1) {
                    // 输出
                    outputStream.write(buffer, 0, numberOfByBytesToWrite);
                }
                // 关闭流
                inputStream.close();
            };
            // 返回的响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");
            headers.add("Content-Disposition", String.format("attachment;filename=\"%s\"", fileName));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "8");
            headers.add("Content-Language", "UTF-8");
            // 返回给浏览器
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);
        } catch (Exception e) {
            log.error("文件下载失败:{}", e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据token拿到用户的数据
     */
    private User getUserByToken(String token) {
        // 拿到用户的数据
        String obj = redisOperator.get(token);
        Assert.notNull(obj, "没有用户的信息");
        return JSONUtil.toBean(obj, User.class);
    }
}
