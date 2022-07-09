package cn.hnit.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.net.URI;

/**
 * HDFS配置
 *
 * @since: 2022/6/10 9:12
 * @author: 梁峰源
 */
@Slf4j
@Configuration
@DependsOn
public class HdfsConfig {
    @Value("${hdfs.path}")
    private String path;
    @Value("${hdfs.nameservices}")
    private String nameservices;
    @Value("${hdfs.username}")
    private String username;
//    @Value("${hdfs.namenodes}")
//    private String[] namenodes;
//    @Value("${hdfs.namenodesAddr}")
//    private String[] namenodesAddr;
    @Value("${hdfs.provider}")
    private String provider;

    private final String[] namenodes = {"nn1","nn2"};
//    private final String[] namenodesAddr = {"node1:8020","node2:8020"};
    private final String[] namenodesAddr = {"hadoop1001:8020","hadoop1002:8020","hadoop1:8020","hadoop2:8020"};


    @Bean("fileSystem")
    public FileSystem createFs(){
        //读取配置文件
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        // 设置文件的副本数，我有三个datenode，这里我设置三份
        conf.set("dfs.replication", "3");
        /*
         * NameNode节点存放的是文件目录，也就是文件夹、文件名称
         * 本地可以通过公网访问 NameNode，所以可以进行文件夹的创建
         * 当上传文件需要写入数据到DataNode时，NameNode 和DataNode 是通过局域网进行通信
         * NameNode返回地址为 DataNode 的私有 IP，本地无法访问
         * 只能放回主机名，通过主机名与公网地址的映射便可以访问到DataNode节点，问题将解决
         */
        conf.set("dfs.client.use.datanode.hostname", "true");
        conf.set("fs.defaultFS", "hdfs://" + nameservices);
        conf.set("dfs.nameservices",nameservices);
        conf.set("dfs.ha.namenodes." + nameservices, namenodes[0]+","+namenodes[1]);
        conf.set("dfs.namenode.rpc-address." + nameservices + "." + namenodes[0], namenodesAddr[0]);
        conf.set("dfs.namenode.rpc-address." + nameservices + "." + namenodes[1], namenodesAddr[1]);
        conf.set("dfs.client.failover.proxy.provider." + nameservices,provider);
        String hdfsRPCUrl = "hdfs://" + nameservices + ":" + 8020;
        // 文件系统
        FileSystem fs = null;
        // 返回指定的文件系统,如果在本地测试，需要使用此种方法获取文件系统
        try {
            URI uri = new URI(hdfsRPCUrl.trim());
            fs = FileSystem.get(uri, conf, username);
        } catch (Exception e) {
            log.error("", e);
        }
        return  fs;
    }
}
