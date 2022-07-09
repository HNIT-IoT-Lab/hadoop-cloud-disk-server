package cn.hnit.service.impl;

import cn.hnit.service.HdfsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @since: 2022/6/10 9:32
 * @author: 梁峰源
 */
@Slf4j
@Service
public class HdfsServiceImpl implements HdfsService {

    @Override
    public List<Map<String, String>> listStatus(String path) {

        return null;
    }
}