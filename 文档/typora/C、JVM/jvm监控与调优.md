![image-20210412171623834](C:\Users\zhangzongchang\AppData\Roaming\Typora\typora-user-images\image-20210412171623834.png)

# 一、性能优化步骤

## 1.1 性能监控

- GC频繁
- cpu load过高
- OOM
- 内存泄漏
- 死锁
- 程序响应时间较长

## 1.2 性能分析

- 打印GC日志，通过GCViewer或者其他工具分析日志信息
- 灵活运用命令行工具：jstack、jmap、jinfo等
- dump出堆文件，使用内存分析工具分析文件
- 使用阿里Arthas、jconsole、JvisualVM实时查看JVM状态
- jstack查看堆栈信息

## 1.3 性能调优

- 适当增加内存，根据业务背景选择垃圾回收期
- 优化代码，控制内存使用
- 增加机器，分散节点压力
- 合理设置线程池现线程数量
- 使用中间件提高程序效率，比如缓存，消息队列等

# 二、性能指标

- 停顿时间（响应时间）
- 吞吐量
  - jvm GC：程序运行时间/（程序运行时间+垃圾回收时间）
- 并发数
- 内存占用

# 三、常用命令

## 3.1 jps

查看正在运行的java进程

```
jps <options>

options:
	-q 仅显示进程号
	-l 输出应用程序主类的全类名
	-m 输出虚拟机启动时传递给main的参数
	-v 输出jvm参数
```

