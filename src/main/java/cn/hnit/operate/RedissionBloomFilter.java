package cn.hnit.operate;

import cn.hnit.utils.IOUtil;
import org.apache.directory.api.util.Hex;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * <p>
 *
 * </p>
 *
 * @since: 2022/6/27 19:46
 * @author: 梁峰源
 */
@Component
public class RedissionBloomFilter {
    // 预期插入数量
    private static final long expectedInsertions = 200L;
    // 误判率
    private static final double falseProbability = 0.01;
    private RBloomFilter<String> bloomFilter = null;
    @Autowired
    private RedissonClient redissonClient;


    @PostConstruct
    public void init() {
        bloomFilter = create("uploadFileBloomFilter", expectedInsertions, falseProbability);
    }

    /**
     * 创建布隆过滤器
     *
     * @param filterName         过滤器名称
     * @param expectedInsertions 预测插入数量
     * @param falsePositiveRate  误判率
     */
    public <T> RBloomFilter<T> create(String filterName, long expectedInsertions, double falsePositiveRate) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.tryInit(expectedInsertions, falsePositiveRate);
        return bloomFilter;
    }

    public boolean fileIsExists(Object file) {
        // 拿到输入里
        InputStream in = null;
        try {
            in = IOUtil.getInputStream(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        assert in != null;
        return fileIsExists(in);
    }

    public boolean fileIsExists(InputStream in) {
        String fileMd5 = getFileMd5(in);
        if (!bloomFilter.contains(fileMd5)) {
            // 加入
            bloomFilter.add(fileMd5);
            return false;
        }
        return true;
    }

    /**
     * 获得输入文件的md5码
     */
    public String getFileMd5(InputStream in) {
        try {
            // 这里需要分段对文件进行hash
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 缓存区，一次读1MB
            byte[] buffer = new byte[1024 * 1024];
            int length = -1;
            while ((length = in.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(md5.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
