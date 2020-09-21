package com.zzc.zookeeper.service;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzc
 * @since 2020-09-21
 */
public class ZkClient {

    private String connectString = "192.168.233.128:2181";
    private int sessionTimeout = 3000;
    ZooKeeper zkCli = null;

    public static void main(String[] args) throws Exception {
        ZkClient zkClient = new ZkClient();
        // 1.获取连接
        zkClient.getConnect();
        // 2.监听服务的节点信息
        zkClient.getServers();

        // 3.业务逻辑（一直监听）
        zkClient.getWatch();
    }

    public void getConnect() throws Exception{
        zkCli = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                List<String> children = null;
                try {
                    // 监听父节点
                    children = zkCli.getChildren("/servers", true);

                    // 创建集合存储服务器列表
                    ArrayList<String> serverList = new ArrayList<String>();

                    // 获取每个节点的数据
                    for (String c : children) {
                        byte[] data = zkCli.getData("/servers/" + c, true, null);
                        serverList.add(new String(data));
                    }
                    System.out.println("getConnect()="+serverList);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getServers() throws Exception{
        List<String> children = zkCli.getChildren("/servers", true);
        ArrayList<String> serverList = new ArrayList<String>();
        // 获取每个节点的数据
        for (String c : children) {
            byte[] data = zkCli.getData("/servers/" + c, true, null);
            serverList.add(new String(data));
        }
        // 打印服务器列表
        System.out.println("getServers()="+serverList);
    }

    // 3.业务逻辑
    public void getWatch() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}
