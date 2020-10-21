[TOC]

# 一、入门概述

## 1. 是什么

> Linux 英文解释为 Linux is not Unix

Linux内核最初只是由芬兰人林纳斯·托瓦兹（Linus Torvalds）在赫尔辛基大学上学时出于个人爱好而编写的。
Linux是一套免费使用和自由传播的类Unix操作系统，是一个基于POSIX和UNIX的多用户、多任务、支持多线程和多CPU的操作系统。Linux能运行主要的UNIX工具软件、应用程序和网络协议。它支持32位和64位硬件。


Linux继承了Unix以网络为核心的设计思想，是一个性能稳定的多用户网络操作系统。

目前市面上较知名的发行版有：Ubuntu、RedHat、CentOS、Debain、Fedora、SuSE、OpenSUSE

## 2.特点

- <strong style="color:red">**linux一切皆文件**</strong>
- linux文件无后缀名一说

## 3. 与windows的区别

![image-20201021180052472](D:\myself\springboot-example\文档\typora\images\linux01.png)

# 二、安装

## 1. 安装centos7教程

https://www.cnblogs.com/yxth/p/11806879.html

## 2.安装问题

### 2.1 本机无法连接虚拟机

在安装教程配置网络时，选择的模式是NAT，注意：配置的ip要和VMnet8的网段一致

>VMnet0 自动桥接模式
>VMnet1 仅主机模式
>VMnet8  NAT模式

 将ONBOOT（no为开机不启动网卡）改成yes，重启网络服务

```
 vi /etc/sysconfig/network-scripts/ifcfg-ens33     
```

![image-20201021180436366](D:\myself\springboot-example\文档\typora\images\linux02.png)

### 2.2 中文乱码

**Vi /etc/sysconfig/i18n** 将字符集改成zh_CN.UTF-8
如果zh_CN.UTF-8不行，就修改成zh_CN

终端secureCRT乱码

![image-20201021181404158](D:\myself\springboot-example\文档\typora\images\linux03.png)

## 3. 安装中配置的三个磁盘分区的作用

**/boot** 内含启动文件和内核。
启动文件：用于决断你需要启动哪个操作系统或者启动哪个内核。
内核：简单的讲，程序与硬件间的桥梁，你使用应用程序通过内核，控制整个计算机。

**SWAP** 作为虚拟内存文件的专门分区。作用类似windows的pagesfile.sys。
你可以单独划这个分区，而用一个文件来代替这个分区。但单独的分区效率会高一些，所以通常都独立划分这个分区。

**/**    根分区，硬盘不论分几个区。所有的文件都在根目录下。
在windows里，你把硬盘分为c： d: e: 啥的，然后分别装系统、程序、游戏、歌曲啥的。在linux下不一样了：不论你分几个区，你都要给他们起个名字，然后用 /games /music /study 这样的名字来挂载访问，和目录完全一样待遇。所以，在linux下，看起来硬盘就像一个分区一样。这个分区的根目录，就是/ 。

# 三、远程登陆与ssh服务

## 1. 安装secureCRT

Linux系统中是通过SSH服务实现的远程登录功能，默认ssh服务端口号为 22。

Window系统上 Linux 远程登录客户端有SecureCRT, Putty, SSH Secure Shell等

## 2. ssh服务命令

```
## 查看
/etc/init.d/sshd

## 启动
/etc/init.d/sshd status

## 重启
/etc/init.d/sshd restart

## 关闭
/etc/init.d/sshd stop
```

# 四、Linux的启动过程/关机

针对的是centos6

## 1. 6个终端

Ctrl+Alt+F1/F2......F6   
Ctrl+Alt+F7,回到图形化界面
who，看看那几个tty连接着

centos7只有两个终端：Ctrl+Alt+F1/F2 （进入/离开图形化界面）

## 2. 如何进入图形化界面

```
## centos7 inittab文件内容为空
cat /etc/inittab 
```

![image-20201021182522238](D:\myself\springboot-example\文档\typora\images\linux04.png)

Linux系统有7个运行级别(runlevel)：常用的是3和5

运行级别0：系统停机状态，系统默认运行级别不能设为0，否则不能正常启动
运行级别1：单用户工作状态，root权限，用于系统维护，禁止远程登陆
运行级别2：多用户状态(没有NFS)，没有网络服务
运行级别3：完全的多用户状态(有NFS)，登陆后进入控制台命令行模式
运行级别4：系统未使用，保留
运行级别5：X11表示控制台，进入图形界面
运行级别6：系统正常关闭并重启，默认运行级别不能设为6，否则不能正常启动

## 3. 启动过程

**（1）内核引导**

​          接通电源BIOS自检，按照BIOS中设置的启动设备（通常是硬盘）来启动，操作系统接管硬件以后，

​          首先读入 /boot 目录下的内核文件。

**（2）运行init**

​          init 进程是系统所有进程的起点，你可以把它比拟成系统所有进程的老祖宗，没有这个进程，系统中任何进程都不会启动。
​          init 程序首先是需要读取配置文件 /etc/inittab

**（3）系统初始化**

​        在init的配置文件中有这么一行： si::sysinit:/etc/rc.d/rc.sysinit　它调用执行了/etc/rc.d/rc.sysinit，

​        而rc.sysinit是一个bash shell的脚本，它主要是完成一些系统初始化的工作，rc.sysinit是每一个运行级别都要首先运行的重要脚本
​        它主要完成的工作有：激活交换分区，检查磁盘，加载硬件模块以及其它一些需要优先执行任务

**（4）建立终端**

**（5）用户登陆**

​         一般来说，用户的登录方式有三种：
​		        命令行登录
​		        ssh登录
​		        图形界面登录

![image-20201021183447446](D:\myself\springboot-example\文档\typora\images\linux05.png)

## 4. 关机

在linux领域内大多用在服务器上，很少遇到关机的操作。毕竟服务器上跑一个服务是永无止境的，除非特殊情况下，不得已才会关机 。

正确的关机流程为：sync > shutdown > reboot > halt

```
sync   将数据由内存同步到硬盘中
shutdown –h 10 ‘This server will shutdown after 10 mins’ 这个命令告诉大家，计算机将在10分钟后关机，并且会显示在登陆用户的当前屏幕中
Shutdown –h now 立马关机
Shutdown –r now 系统立马重启
reboot 就是重启，等同于 shutdown –r now
halt 关闭系统，等同于shutdown –h now 和 poweroff
```

<strong style="color:red">最后总结一下，不管是重启系统还是关闭系统，首先要运行sync命令，把内存中的数据写到磁盘中。</strong>

# 五、VI/VIM编辑器

Vim 具有程序编辑的能力，可以主动的以字体颜色辨别语法的正确性，方便程序设计。
Vim是从 vi 发展出来的一个文本编辑器。代码补完、编译及错误跳转等方便编程的功能特别丰富，在程序员中被广泛使用。

## 1. 一般模式

以 vi 打开一个档案就直接进入一般模式了(这是默认的模式)。
在这个模式中， 你可以使用『上下左右』按键来移动光标，你可以使用『删除字符』或『删除整行』来处理档案内容， 也可以使用『复制、贴上』来处理你的文件数据

![image-20201021190408269](D:\myself\springboot-example\文档\typora\images\linux06.png)

## 2.编辑模式

『i, I, o, O, a, A, r, R』进入编辑模式
 esc 退出回到一般模式

## 3. 指令模式

```
:w   write写入
:q   quit退出
:!   强制执行
:wq! 强制保存并退出vi
/    查找，/被查找词，n是查找下一个，shift+n是往上查找
?    查找，?被查找词，n是查找上一个，shift+n是往下查找
:set nu 显示行号
:set nonu 取消显示行号
```

![image-20201021190819346](D:\myself\springboot-example\文档\typora\images\linux07.png)