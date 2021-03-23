单独使用mybatis是有很多限制的（`比如无法实现跨越多个session的事务`）

很多业务系统本来就是使用spring来管理的事务，因此mybatis最好与spring集成起来使用。

https://my.oschina.net/xianggao/blog/551161