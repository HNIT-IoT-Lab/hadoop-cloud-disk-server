package cn.hnit.entity.Vo;

import cn.hnit.entity.UserLogAnalyse;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/7/6 17:41
 * @author: 梁峰源
 */
@Data
@Accessors(chain = true)
public class UserLogAnalyseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int QueryNetworkNum;
    private int LoginNum;
    private int uploadNum;
    private int downLoadNum;

    public UserLogAnalyseVo(List<UserLogAnalyse> list) {
        list.forEach(e ->{
            switch (e.getUserOperate().trim()) {
                case "用户查询网盘信息":
                    this.QueryNetworkNum = e.getNum();
                    break;
                case "登录操作":
                    this.LoginNum = e.getNum();
                    break;
                case "用户上传文件":
                    this.uploadNum = e.getNum();
                    break;
                case "用户下载文件":
                    this.downLoadNum = e.getNum();
                    break;
            }
        });
    }
}
