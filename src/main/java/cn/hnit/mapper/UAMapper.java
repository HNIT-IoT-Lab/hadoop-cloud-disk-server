package cn.hnit.mapper;

import cn.hnit.entity.UserLogAnalyse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户业务日志分析
 * </p>
 *
 * @since: 2022/7/6 17:44
 * @author: 梁峰源
 */
public interface UAMapper extends BaseMapper<UserLogAnalyse> {
    @Select("select * from user_logs_analyse where user_name = #{userName}")
    List<UserLogAnalyse> listLog(String userName);
}
