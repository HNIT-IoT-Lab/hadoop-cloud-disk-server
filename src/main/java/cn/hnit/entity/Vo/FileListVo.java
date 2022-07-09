package cn.hnit.entity.Vo;

import cn.hnit.entity.Do.FileStatusDo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description:
 * @since: 2022/6/10 14:33
 * @author: 梁峰源
 */
@Data
@Accessors(chain = true)
public class FileListVo {
    /**
     * 第几页
     */
    private Integer pageNo;
    /**
     * 显示多少条数据
     */
    private Integer pageSize;
    /**
     * 共多少条数据
     */
    private Integer Total;

    /**
     * 文件数
     */
    private Long fileNum;

    /**
     * 目录数
     */
    private Long listNum;

    /**
     * 返回的数据集合
     */
    public List<FileStatusDo> fileStatusDos;


}
