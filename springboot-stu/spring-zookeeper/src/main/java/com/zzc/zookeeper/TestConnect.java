package com.zzc.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

/**
 * @author zzc
 * @since 2020-09-21
 */
public class TestConnect {

    private String connectString = "192.168.233.128:2181";
    private int sessionTimeout = 3000;
    ZooKeeper zkCli = null;
    List<String> children = null;

    public  void init() throws Exception {
        zkCli = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            // 回调监听
            @Override
            public void process(WatchedEvent event) {
                // System.out.println(event.getPath() + "\t" + event.getState() + "\t" + event.getType());
                try {
                    List<String> children = zkCli.getChildren("/", true);
                    for (String c : children) {
                         System.out.println("children="+ c);
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        byte[] data = zkCli.getData("/hello", new Watcher() {
            // 监听的具体内容
            @Override
            public void process(WatchedEvent event) {
                System.out.println("监听路径为：" + event.getPath());
                System.out.println("监听的类型为：" + event.getType());
                System.out.println("监听被修改了！！！");
            }
        }, null);
        // 监听目录
        children = zkCli.getChildren("/", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("监听路径为：" + event.getPath());
                System.out.println("监听的类型为：" + event.getType());
                System.out.println("监听被修改了！！！");
                for (String c : children) {
                    System.out.println(c);
                }
            }
        });
    }

    /**创建子节点
     * ZooDefs.Ids
     *    1.OPEN_ACL_UNSAFE  : 完全开放的ACL，任何连接的客户端都可以操作该属性znode
     *    2.CREATOR_ALL_ACL : 只有创建者才有ACL权限
     *    3.READ_ACL_UNSAFE：只能读取ACL
     *
     * CreateMode
     *    1.PERSISTENT--持久型
     *    2.PERSISTENT_SEQUENTIAL--持久顺序型
     *    3.EPHEMERAL--临时型
     *    4.EPHEMERAL_SEQUENTIAL--临时顺序型
     */
    public void createZnode() throws KeeperException, InterruptedException {
        String path = zkCli.create("/hello", "world".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(path);
    }

    public void getChild() throws KeeperException, InterruptedException {
        List<String> children = zkCli.getChildren("/", true);
        for (String c : children) {
            System.out.println(c);
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    // 删除节点
    public void rmChildData() throws KeeperException, InterruptedException {
        // byte[] data = zkCli.getData("/bbq", true, null);
        // System.out.println(new String(data));
        zkCli.delete("/hello", -1);
    }

    // 修改数据
    public void setData() throws KeeperException, InterruptedException {
        zkCli.setData("/hello", "17".getBytes(), -1);
    }

    // 判断节点是否存在
    public void testExist() throws KeeperException, InterruptedException {
        Stat exists = zkCli.exists("/hello", false);
        System.out.println(exists == null ? "not exists" : "exists");
    }

    public static void main(String[] args) {
        TestConnect connect = new TestConnect();
        try {
            connect.init();
//            connect.testExist();
//            connect.createZnode();
            connect.getChild();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
