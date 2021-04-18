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

Java Process Status

**查看正在运行的java进程**

```
jps <options> <hostid>

options:
	-q 仅显示进程号
	-l 输出应用程序主类的全类名
	-m 输出虚拟机启动时传递给main的参数
	-v 输出jvm参数
	
hostid
	RMI注册表中注册的主机名
```

## 3.2 jstat

JVM Statistics Monitoring Tool

**查看jvm统计信息**。用于监视虚拟机各种运行状态信息的命令行工具。如：类装载、内存、垃圾收集、JIT编译等运行数据

```xml
jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]

查看命令相关参数：
	jstat -h | -help
	
option:
  -类装载相关的:
	-class: 显示Classloader的相关信息:类的装载、卸载数量、总空间、类装载所消耗的时间等
  -垃圾回收相关的:
	-gc: 显示与GC相关的堆信息。包括Eden区、两个Survivor区、老年代、永久代等的容量、己用空间、GC时间合计等信息。
	-gccapacity: 显示内容与-gc基本相同，但输出主要关注Java堆各个区域使用到的最大、最小空间。
	-gcutil: 显示内容与-gc基本相同，但输出主要关注已使用空间占总空间的百分比。
	-gccause: 与-gcutil功能一样，但是会额外输出导致最后一次或当前正在发生的GC产生的原因。
	-gcnew: 显示新生代GC状况
	-gcnewcapacity: 显示内容与-gcnew基本相同，输出主要关注使用到的最大、最小空间
    -geold:显示老年代GC状况
    
interval:
  用于指定输出统计数据的周期，单位为ms，即：查询间隔

count: 查询总次数

-t:显示程序的运行时间，我们可以比较java进程的启动时间以及总GC时间，或者两次测量的间隔时间以及总GC时间的增量，来得出GC时间占运行时间的比例    
    
-h：可以在周期性数据输出时，输出多少行数据后输出一个表头信息
```

## 3.3 jinfo

Configuration Info for Java

**实时查看和修改jvm配置参数**

```
jinfo [option] pid
```

![image-20210418141509456](D:\myself\springboot-example\文档\typora\images\jvm61.png)

修改是实时生效的，但并非所有的配置都支持动态修改，查看被标记为manageable（支持动态修改的）的参数:

```
java -XX:PrintFlagsFinal -version | grep manageable
```

拓展

```
java -XX:+PrintFlagsInitial 查看所有JVM参数启动的初始值
java -XX:+PrintFlagsFinal 查看所有JVM参数的最终值
java -XX:+PrintCommandLineFlags 查看那些已经被用户或者JVM设置过的详细的参数的名称和值
```

## 3.4 jmap

JVM Memroy Map

**导出内存映像文件&内存使用情况**

```
jmap <option>

option:
	-dump 生成java堆转储快照
	  	-dump:live 只保存堆中的存活对象
	-heap
		输出整个堆空间的详细信息，包括GC的使用、堆配置信息，以及内存的使用信息等
	-hista
		输出堆中对象的统计信息，包括类、实例数量和合计容量
		特别的:-histo:live只统计堆中的存活对象
	-permstat
		以ClassLoader为统计口径输出永久代的内存状态信息
	-finalizerinfo
		显示在F-Queue中等待Finalizer线程执行finalize方法的对象
	-F
		当虚拟机进程对-dump选项没有任何响应时，可使用此选项强制执行生成dump文件
	-h|-help
		jmap工具使用的帮助命令
	-J <flag>
		传递参数给jmap启动的jvm
```

导出内存映像文件：

```
手动方式
	jmap -dump:format=b,file=<filename.hprof> <pid>
	jmap -dump:live,format=b,file=<filename.hprof> <pid>

自动方式
	-XX:+HeapDumpOnOutOfMemoryError
	-XX:HeapDumpPath=<filename.hprof>
```

显示堆内存相关信息

```
jmap -heap pid 显示该时刻堆中各个区域内存使用情况
jmap -histo pid 显示该时刻队中各个对象的个数及内存占用情况
```

## 3.5 jhat

JVM Heap Analysis Tool

![image-20210418144426549](D:\myself\springboot-example\文档\typora\images\jvm62.png)

## 3.6 jstack

**打印JVM中的线程快照**

生成线程快照的作用:可用于定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等问题。这些都是导致线程长时间停顿的常见原因。当线程出现停顿时，就可以用jstack显示各个线程调用的堆栈情况。

在thread dump中，要留意下面几种状态

- 死锁，Deadlock(重点关注)
- 等待资源，Waiting on condition(重点关注)
- 等待获取监视器，Waiting on monitor entry(重点关注)
- 阻塞，Blocked(重点关注)
- 执行中，Runnable
- 暂停，Suspended

```
jstack [option] <pid>

option:
	-F 当正常输出的请求不被响应时，强制输出线程堆栈
	-l 除堆栈外，显示关于锁的附加信息
	-m 如果调用到本地方法，可以显示C、C++的堆栈
	-h 帮助操作
```

## 3.7 jcmd

可以用来实现前面除了jstat之外所有命令的功能。

```
jcmd -l 列出所有的JVM进程
jcmd pid help
```

## 3.8 jstatd

远程主机信息收集

# 四、GUI工具

## 4.1 JConsole

java自带

## 4.2 Visual VM

java自带，也可自行安装

主要功能：

- 生成/读取堆内存快照
- 查看JVM参数和系统属性
- 查看运行中的虚拟机进程
- 生成/读取线程快照
- 程序资源的实时监控
- JMX代理连接、远程环境监控、CPU分析和内存分析	

## 4.3 eclipse MAT

## 4.4 Jprofiler

## 4.5 Arthas

## 4.6 Java Mission Control

## 4.7 Btrace

## 4.8 Flame Graphs 火焰图















