package cn.hnit.entity;

import cn.hnit.core.FileTree;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author 梁峰源
 * @since 2022-06-08
 */
@Data
@ApiModel(value = "用户实体类")
@TableName(autoResultMap = true) // 将字段自动转为json
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "uid", type = IdType.AUTO)
    @ApiModelProperty(name="uid",value = "用户id",required = true,example = "1")
    private Integer uid;

    @ApiModelProperty(name="uname",value = "用户名",required = true,example = "张三")
    private String uname;

    @ApiModelProperty(name="upwd",value = "用户密码",required = true,example = "upwd")
    private String upwd;

    @ApiModelProperty(name="avatar",value = "用户头像",required = true,example = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
    private String avatar;

    @ApiModelProperty(name="role",value = "用户权限",required = true,example = "ROLE_ADMIN")
    private String role;


    @ApiModelProperty(name="fileTree",value = "用户拥有的文档树",required = true,example = "List<Map<int,List<...>>>")
    @TableField(typeHandler = JacksonTypeHandler.class) // 以json的格式存入数据库
    private FileTree fileTree;

    @ApiModelProperty(hidden = true)
    @TableField(exist = false)
    private List<String> roles;

    public User() {
    }

    public User(String uname, String upwd, String role) {
        this.uname = uname;
        this.upwd = upwd;
        this.role = role;
    }

    public static class role {
        public static final String ROLE_USER = "ROLE_USER";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
    }
}
