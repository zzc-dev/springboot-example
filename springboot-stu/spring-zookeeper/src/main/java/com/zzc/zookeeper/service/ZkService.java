package com.zzc.zookeeper.service;

import org.apache.zookeeper.*;

/**
 * @author zzc
 * @since 2020-09-21
 */
public class ZkService {

    private String connectString = "192.168.233.128:2181";
    private int sessionTimeout = 3000;
    ZooKeeper zkCli = null;

    // 定义父节点
    private String parentNode = "/servers";

    public static void main(String[] args) throws Exception{
        ZkService zkService = new ZkService();
        // 1.连接Zookeeper
        zkService.getConnect();
        // 2.注册节点信息 服务器ip添加到zk中
        zkService.regist(args[0]);
        // 3.业务逻辑处理
        zkService.build(args[0]);
    }

    public void getConnect() throws Exception{
        zkCli = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("watchedEvent"+ watchedEvent);
            }
        });
    }

    public void regist(String hostname) throws Exception{
        String node = zkCli.create(parentNode + "/server", hostname.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("regist="+node);
    }

    public void build(String hostname) throws Exception{
        System.out.println("服务器上线了"+hostname);
        Thread.sleep(Long.MAX_VALUE);
    }
}
