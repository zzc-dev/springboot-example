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
修改方式：LANG="zh_CN.UTF-8"

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

# 六、用户与用户组

## 1. 概述

Linux系统是一个多用户多任务的分时操作系统，任何一个要使用系统资源的用户，都必须首先向系统管理员申请一个账号，然后以这个账号的身份进入系统。

用户的账号一方面可以帮助系统管理员对使用系统的用户进行跟踪，并控制他们对系统资源的访问；
另一方面也可以帮助用户组织文件，并为用户提供安全性保护。每个用户账号都拥有一个惟一的用户名和各自的口令。

用户在登录时键入正确的用户名和口令后，就能够进入系统和自己的主目录。
实现用户账号的管理，要完成的工作主要有如下几个方面：

用户账号的添加、删除与修改。
用户口令的管理。
用户组的管理。

## 2. 用户

```
useradd 用户名
password 用户名   # 根据提示输入密码
id 用户名         # 是否存在该用户
whoami/who am i
su - 用户名       # 切换用户名。root用户输入是'#'，普通用户输入是'$'
userdel [-r]     # -r 将用户和用户主目录【/home/lisi】都删除
usermod -g 用户组 用户名         
```

## 3. 用户组

每个用户都有一个用户组，系统可以对一个用户组中的所有用户进行集中管理。不同Linux 系统对用户组的规定有所不同，

如Linux下的用户属于与它同名的用户组，这个用户组在创建用户时同时创建。

用户组的管理涉及用户组的添加、删除和修改。组的增加、删除和修改实际上就是对/etc/group文件的更新。

```
groupadd 组名
groupdel 组名
groupmod -n 新组名 老组名
```

## 4. 与用户有关的系统文件

完成用户管理的工作有许多种方法，但是每一种方法实际上都是对有关的系统文件进行修改。
与用户和用户组相关的信息都存放在一些系统文件中，
这些文件包括/etc/passwd, /etc/shadow, /etc/group等。

**/etc/password**

Linux系统中的每个用户都在/etc/passwd文件中有一个对应的记录行，它记录了这个用户的一些基本属性。

> 格式：用户名:口令:用户标识号:组标识号:注释性描述:主目录:登录Shell

**/etc/shadow**

由于/etc/passwd文件是所有用户都可读的，如果用户的密码太简单或规律比较明显的话，一台普通的计算机就能够很容易地将它破解，因此对安全性要求较高的Linux系统都把加密后的口令字分离出来，单独存放在一个文件中，这个文件是/etc/shadow文件。 

只有超级用户才拥有该文件读权限，这就保证了用户密码的安全性。

> 登录名:加密口令:最后一次修改时间:最小时间间隔:最大时间间隔:警告时间:不活动时间:失效时间:标志

**/etc/group**

用户组的所有信息都存放在/etc/group文件中

> 组名:口令:组标识号:组内用户列表

# 七、常用基本命令

可以使用 man 命令去辅助了解具体的命令，如man date

## 1. 时间日期类

```
date：
	date "+%Y-%M-%D"
cal:
	cal 2020
```

## 2. 文件目录类

```
pwd            # print work directory
ls
   ls -l       # 相当于ll，以列表的形式展示当前目录的所有文件和目录
   ls -R       # 递归展示所有的目录和文件
mkdir [-p]     # make dir  -p:创建父目录同时创建子目录
rmdir          # remove dir 只能删除一个空的目录
touch 1.txt    # 创建文件
cd             # change dir
   cd ~        # 回到/root
cp 旧 新        # 可以拷贝文件或目录
rm
   rm -i
   rm -R
   rm -rf      # 强制删除目录或文件
mv 旧 新

cat
   -A ：相当於 -vET 的整合选项，可列出一些特殊字符而不是空白而已；
   -b ：列出行号，仅针对非空白行做行号显示，空白行不标行号！
   -E ：将结尾的断行字节 $ 显示出来；
   -n ：列印出行号，连同空白行也会有行号，与 -b 的选项不同；
   -T ：将 [tab] 按键以 ^I 显示出来；
   -v ：列出一些看不出来的特殊字符
tac

more  # 一页一页的显示文件内容
    空白键 (space)：代表向下翻一页；
    Enter       ：代表向下翻『一行』；
    q           ：代表立刻离开 more ，不再显示该文件内容。
    b 或 [ctrl]-b ：代表往回翻页，不过这动作只对文件有用，对管线无用。
less

head -n 10 1.txt # 查看前10行
tail -n 10 1.txt # 查看后10行
history          # 查看历史查询命令
```

## 3. 文件权限类



## 4. 磁盘分区类

### 分区

   在终端中查看linux有几个磁盘分区命令：fdisk -l

![image-20201022141147394](D:\myself\springboot-example\文档\typora\images\linux08.png)

s表示scsi硬盘
d表示disk
a表示第一块盘(a为基本盘(第一块硬盘，两块就会有b)，b为基本从属盘，c为辅助主盘，d为辅助从属盘)
数字，前四个分区用1~4表示，它们是主分区或扩展分区，从5开始才是逻辑分区

### **挂载**

对于Linux用户来讲，不论有几个分区，分别分给哪一个目录使用，它总归就是一个根目录、一个独立且唯一的文件结构Linux中每个分区都是用来组成整个文件系统的一部分，她在用一种叫做“挂载”的处理方法，它整个文件系统中包含了一整套的文件和目录，并将一个分区和一个目录联系起来，要载入的那个分区将使它的存储空间在这个目录下获得

**mount 【参数】  设备名称   落地挂载点目录**

mount umount 

![image-20201022142325388](D:\myself\springboot-example\文档\typora\images\linux09.png)

### 硬盘

**df -h**
列出文件系统的整体磁盘使用量,检查文件系统的磁盘空间占用情况

选项与参数：
-a ：列出所有的文件系统，包括系统特有的 /proc 等文件系统；
-k ：以 KBytes 的容量显示各文件系统；
-m ：以 MBytes 的容量显示各文件系统；
-h ：以人们较易阅读的 GBytes, MBytes, KBytes 等格式自行显示；
-H ：以 M=1000K 取代 M=1024K 的进位方式；
-T ：显示文件系统类型, 连同该 partition 的 filesystem 名称 (例如 ext3) 也列出；
-i ：不用硬盘容量，而以 inode 的数量来显示

## 5. 搜索查找类

**find**

```
解释： 查找文件或者目录
命令：find+搜索路径+参数+搜索关键字

按文件名：find /home/esop -name  't*'

按拥有者：find /home/esop -user esop
```

**grep**

```
在文件内搜索字符串匹配的行并输出
grep+参数+查找内容+源文件
 
参数：
－c：只输出匹配行的计数。
－I：不区分大小写(只适用于单字符)。
－h：查询多文件时不显示文件名。
－l：查询多文件时只输出包含匹配字符的文件名。
－n：显示匹配行及 行号。
－s：不显示不存在或无匹配文本的错误信息。
－v：显示不包含匹配文本的所有行。
```

## 6. 进程线程类

**ps**

![image-20201022162342543](D:\myself\springboot-example\文档\typora\images\linux10.png)

ps -aux,然后再利用一个管道符号导向到grep去查找特定的进程,然后再对特定的进程进行操作

ps -ef是以全格式显示当前所有的进程
      -e 显示所有进程。
     -f 全格式。

**netstat**

![image-20201022162503378](D:\myself\springboot-example\文档\typora\images\linux11.png)

![img](D:\myself\springboot-example\文档\typora\images\linux12.png)

## 7. 压缩和解压类

gzip                               压缩  改变源文件
gunzip  文件.gz           解压缩 改变源文件

zip
unzip

```
tar        # 打包目录,压缩后的文件格式.tar.gz
参数：
-c 产生.tar打包文件
-v 显示详细信息
-f 指定压缩后的文件名
-z 打包同时压缩
-x 解包.tar文件
```

**压缩：tar -zcvf  XXX.tar.gz   n1.txt    n2.txt**
**解压：tar -zxvf  XXX.tar.gz**

# 八、Linux文件与目录结构

## 1. 树状目录结构

![image-20201022172936728](D:\myself\springboot-example\文档\typora\images\linux13.png)

/bin       可执行二进制文件命令的目录，如cat、cp等
/sbin      s就是Super User的意思，这里存放的是系统管理员使用的系统管理程序
/boot    这里存放的是启动Linux时使用的一些核心文件，包括一些连接文件以及镜像文件
/etc       所有的系统管理所需要的配置文件和子目录
/home  存放普通用户的主目录，在Linux中每个用户都有一个自己的目录，一般该目录名是以用户的账号命名的
/mnt     系统提供该目录是为了让用户临时挂载别的文件系统的，我们可以将光驱挂载在/mnt/上，然后进入该目录就可以查看光驱里的内容了。
/opt       这是给主机额外安装软件所摆放的目录。比如你安装一个ORACLE数据库则就可以放到这个目录下。默认是空的。
/root     该目录为系统管理员，也称作超级权限者的用户主目录。
/tmp     这个目录是用来存放一些临时文件的。
/usr       这是一个非常重要的目录，用户的很多应用程序和文件都放在这个目录下，类似与windows下的program 
/var       这个目录中存放着在不断扩充着的东西，我们习惯将那些经常被修改的目录放在这个目录下。包括各种日志文件。

/dev      Device(设备)的缩写,该目录下存放的是Linux的外部设备，在Linux中访问设备的方式和访问文件的方式是相同的
/lib        系统开机所需要最基本的动态连接共享库，其作用类似于Windows里的DLL文件。几乎所有的应用程序都需要用到这些共享库
/lost+found   这个目录一般情况下是空的，当系统非法关机后，这里就存放了一些文件
/media  linux系统会自动识别一些设备，例如U盘、光驱等等，当识别后，linux会把识别的设备挂载到这个目录下。
/proc     这个目录是一个虚拟的目录，它是系统内存的映射，我们可以通过直接访问这个目录来获取系统信息。
/selinux 这个目录是Redhat/CentOS所特有的目录，Selinux是一个安全机制，类似于windows的防火墙
/srv：service缩写，该目录存放一些服务启动之后需要提取的数据。这是linux2.6内核的一个很大的变化。该目录下安装了2.6内核中新出现的一个文件系统 

## 2. 文件属性

Linux系统是一种典型的多用户系统，不同的用户处于不同的地位，拥有不同的权限。
为了保护系统的安全性，Linux系统对不同的用户访问同一文件（包括目录文件）的权限做了不同的规定。

在Linux中我们可以使用ll或者ls –l命令来显示一个文件的属性以及文件所属的用户和组

![](E:\workspace\git\springboot-example\文档\typora\images\image-20201022191944633.png)

```
从左到右的10个字符表示：
以三个为一组，且均为『rwx』 的三个参数的组合。
其中，[ r ]代表可读(read)、[ w ]代表可写(write)、[ x ]代表可执行(execute)。 要注意的是，这三个权限的位置不会改变，
如果没有权限，就会出现减号[ - ]而已。从左至右用0-9这些数字来表示:
第0位确定文件类型，
第1-3位确定属主（该文件的所有者）拥有该文件的权限。---User
第4-6位确定属组（所有者的同组用户）拥有该文件的权限，---Group
第7-9位确定其他用户拥有该文件的权限 ---
```

作用到文件：
[ r ]代表可读(read): 可以读取,查看
[ w ]代表可写(write): 可以修改,但是不代表可以删除该文件,删除一个文件的前提条件是对该文件所在的目录有写权限，才能删除该文件.
[ x ]代表可执行(execute):可以被系统执行

作用到目录：
[ r ]代表可读(read): 可以读取，ls查看目录内容
[ w ]代表可写(write): 可以修改,目录内创建+删除+重命名目录
[ x ]代表可执行(execute):可以进入该目录

## 3.操作文件权限

**chmod**

改变文件或者目录权限
文件: r-查看；w-修改；x-执行文件
目录: r-列出目录内容；w-在目录中创建和删除；x-进入目录

<strong style="color:red">删除一个文件的前提条件:该文件所在的目录有写权限，你才能删除该文件。</strong>

```
chmod 777 文件/目录  777：rwx rwx rwx
                        111 111 111
                 可通过这种二进制方式修改权限
```

**chgrp  组名  文件/目录**

**chown  用户名   文件名 **

**umask**

查看创建文件、目录的默认权限，缺省创建的文件不能授予可执行权限x

![image-20201022192658847](E:\workspace\git\springboot-example\文档\typora\images\image-20201022192658847.png)

默认规则：
 文件是666 减去 022等于644，
   十进制的6等于二进制的110，所以第一组就是rw-
   十进制的4等于二进制的100,  所以第二组就是r--
   十进制的4等于二进制的100,  所以第三组就是r--

 目录是777 减去 022等于755，

