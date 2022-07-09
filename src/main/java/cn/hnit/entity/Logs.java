package cn.hnit.entity;

import cn.hnit.entity.common.DataFormats;
import cn.hutool.http.useragent.OS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 日志实体类
 * </p>
 *
 * @author bxystart
 * @since 2021-04-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("user_logs")
public class Logs implements Serializable {

    private static final long serialVersionUID = 131211331123L;

    @TableId(value = "lid", type = IdType.AUTO)
    private Integer lid;

    /**
     * 登陆用户
     */
    private String uname;

    /**
     * 登陆时间
     */
    @TableField("ltime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = DataFormats.VO_FORMAT)
    private LocalDateTime ltime;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 日志内容
     */
    private String content;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统类型
     */
    private String os;

    enum TYPE {
        LOGIN,LAYOUT,UPLOAD,DOWnLOAD
    }

    public Logs(String uname, LocalDateTime ltime, String ip, String content, String type, String browser, String os) {
        this.uname = uname;
        this.ltime = ltime;
        this.ip = ip;
        this.content = content;
        this.type = type;
        this.browser = browser;
        this.os = os;
    }
}
