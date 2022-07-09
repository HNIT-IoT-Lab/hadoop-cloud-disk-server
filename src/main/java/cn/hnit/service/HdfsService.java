package cn.hnit.service;

import java.util.List;
import java.util.Map;

/**
 * hdfs接口
 *
 * @since: 2022年6月10日09:10:43
 * @author: 梁峰源
 */
public interface HdfsService {
    /**
     * 查看指定路径下的文件列表
     */
    List<Map<String,String>> listStatus(String path);
}
