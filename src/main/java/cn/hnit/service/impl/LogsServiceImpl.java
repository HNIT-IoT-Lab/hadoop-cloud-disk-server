package cn.hnit.service.impl;


import cn.hnit.entity.Logs;
import cn.hnit.entity.User;
import cn.hnit.mapper.LogsMapper;
import cn.hnit.mapper.UserMapper;
import cn.hnit.service.LogsService;
import cn.hnit.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 梁峰源
 * @since 2022-06-08
 */
@Service
public class LogsServiceImpl extends ServiceImpl<LogsMapper, Logs> implements LogsService {

}
