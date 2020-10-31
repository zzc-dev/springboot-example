# 一、JVM体系结构概述

![image-20201030083700650](D:\myself\springboot-example\文档\typora\images\jvm01.png)

## 1. ClassLoader

​	负责加载class文件，class文件在文件开头有特定的文件标示，将class文件字节码内容加载到内存中，并将这些内容转换成方法区中的运行时数据结构
​    ClassLoader只负责class文件的加载，至于它是否可以运行，则由Execution Engine决定 

### 1.1 分类

![image-20201030084107145](D:\myself\springboot-example\文档\typora\images\jvm02.png)

​           java 自带的classLoader：

​					Bootstrap ClassLoader  : 启动类加载器，C++实现，加载java.* 下的类
​                    Extension ClassLoader  : 扩展类加载器，Java实现，加载javax.* 下的类
​                    Application ClassLoader: 系统类加载器，加载当前应用的classpath的所有类

​         用户自定义

​					java.lang.ClassLoader的子类，用户可以定制类的加载方式

### 1.2 双亲委派

当一个类收到了类加载请求，他首先不会尝试自己去加载这个类，而是把这个请求委派给父类去完成，每一个层次类加载器都是如此，因此所有的加载请求都应该传送到启动类加载其中，只有当父类加载器反馈自己无法完成这个请求的时候（在它的加载路径下没有找到所需加载的Class），子类加载器才会尝试自己去加载。

采用双亲委派的一个好处是比如加载位于 rt.jar 包中的类 java.lang.Object，不管是哪个加载器加载这个类，最终都是委托给顶层的启动类加载器进行加载，这样就保证了使用不同的类加载器最终得到的都是同样一个 Object对象。  

### 1.3  沙箱安全机制

## 2. Execution Engine

**执行引擎负责解释命令，提交操作系统执行。** 

## 3. Native Method Stack

**本地接口**的作用是融合不同的编程语言为 Java 所用，它的初衷是融合 C/C++程序，Java 诞生的时候是 C/C++横行的时候，要想立足，必须有调用 C/C++程序，于是就在内存中专门开辟了一块区域处理标记为native的代码，它的具体做法是 **Native Method Stack中登记 native方法，在Execution Engine 执行时加载native libraies。**

 目前该方法使用的越来越少了，除非是与硬件有关的应用，比如通过Java程序驱动打印机或者Java系统管理生产设备，在企业级应用中已经比较少见。因为现在的异构领域间的通信很发达，比如可以使用 Socket通信，也可以使用Web Service等等，不多做介绍。

## 4. Program Counter Register

**PC寄存器**

每个线程都有一个程序计数器，是<strong style="color:red">线程私有</strong>的
就是一个指针，指向方法区中的方法字节码（用来存储指向下一条指令的地址,也即将要执行的指令代码），由执行引擎读取下一条指令，是一个非常小的内存空间，几乎可以忽略不记。

如果执行的是一个Native方法，那这个计数器是空的。

用以完成分支、循环、跳转、异常处理、线程恢复等基础功能。不会发生内存溢出(OutOfMemory=OOM)错误

## 5. Method Area

又叫静态区，存放所有的①类（class），②静态变量（static变量），③静态方法，④常量和⑤成员方法(就是普通方法，由访问修饰符，返回值类型，类名和方法体组成)。

## 6. Stack

栈也叫栈内存，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放，**对于栈来说不存在垃圾回收问题**，只要线程一结束该栈就Over，生命周期和线程一致，是线程私有的。8种基本类型的变量+对象的引用变量+实例方法都是在函数的栈内存中分配。

**先入后出**

栈帧中主要保存3 类数据：

- 本地变量（Local Variables）:输入参数和输出参数以及方法内的变量；
- 栈操作（Operand Stack）:记录出栈、入栈的操作；
- 栈帧数据（Frame Data）:包括类文件、方法等等

>java中叫方法，方法在jvm入栈后叫栈帧，场景不同，叫法不同

每个方法执行的同时都会创建一个栈帧，用于存储局部变量表、操作数栈、动态链接、方法出口等信息，每一个方法从调用直至执行完毕的过程，就对应着一个栈帧在虚拟机中入栈到出栈的过程。栈的大小和具体JVM的实现有关，通常在256K~756K之间,与等于1Mb左右。

![image-20201031092451168](D:\myself\springboot-example\文档\typora\images\jvm03.png)

栈内存溢出：Exception in thread "main" java.lang.StackOverflowError

## **7.Heap

​    ![image-20201031093113119](D:\myself\springboot-example\文档\typora\images\jvm04.png)

### 7.1 堆内部分类

逻辑分类：新生区+老年区+永久区（jdk1.7）/元空间（jdk1.8）

物理分类：新生区+老年区

新生区是类的诞生、成长、消亡的区域，一个类在这里产生，应用，最后被垃圾回收器收集，结束生命。

新生区又分为两部分： 伊甸区（Eden space）和幸存者区（Survivor pace） ，所有的类都是在伊甸区被new出来的。幸存区有两个： 0区（Survivor 0 space）和1区（Survivor 1 space）。

### 7.2 垃圾回收机制（简易版）

> 当伊甸园的空间用完时，程序又需要创建对象，JVM的垃圾回收器将对伊甸园区进行垃圾回收(Minor GC)，将伊甸园区中的不再被其他对象所引用的对象进行销毁。然后将伊甸园中的剩余对象移动到幸存 0区。若幸存 0区也满了，再对该区进行垃圾回收，然后移动到 1 区。那如果1 区也满了呢？再移动到养老区。若养老区也满了，那么这个时候将产生MajorGC（FullGC），进行养老区的内存清理。若养老区执行了Full GC之后发现依然无法进行对象的保存，就会产生OOM异常“OutOfMemoryError”。

**如果出现java.lang.OutOfMemoryError: Java heap space异常，说明Java虚拟机的堆内存不够。原因有二：**

**（1）Java虚拟机的堆内存设置不够，可以通过参数-Xms、-Xmx来调整。**

**（2）代码中创建了大量大对象，并且长时间不能被垃圾收集器收集（存在被引用）。**

### 7.3 轻量级GC（新生代GC）

![image-20201031162547446](D:\myself\springboot-example\文档\typora\images\jvm05.png)

<strong style="color:red">复制 -> 清空 -> 互换</strong>

**1：eden、SurvivorFrom 复制到 SurvivorTo，年龄+1** 

首先，当Eden区满的时候会触发第一次GC,把还活着的对象拷贝到SurvivorFrom区，当Eden区再次触发GC的时候会扫描Eden区和From区域,对这两个区域进行垃圾回收，经过这次回收后还存活的对象,则直接复制到To区域（如果有对象的年龄已经达到了老年的标准，则赋值到老年代区），同时把这些对象的年龄+1

**2：清空 eden、SurvivorFrom** 

然后，清空Eden和SurvivorFrom中的对象，也即复制之后有交换，谁空谁是to

**3：SurvivorTo和 SurvivorFrom 互换** 

最后，SurvivorTo和SurvivorFrom互换，原SurvivorTo成为下一次GC时的SurvivorFrom区。部分对象会在From和To区域中复制来复制去,如此交换15次(由JVM参数`MaxTenuringThreshold`决定,这个参数默认是15),最终如果还是存活,就存入到老年代

### 7.4 永久区 or 元空间

实际而言，方法区（Method Area）和堆一样，是各个线程共享的内存区域，它用于存储虚拟机加载的：类信息+普通常量+静态常量+编译器编译后的代码等等，虽然JVM规范将方法区描述为堆的一个逻辑部分，但它却还有一个别名叫做Non-Heap(非堆)，目的就是要和堆分开。

 对于**HotSpot虚拟机**（sun公司的jvm产品），很多开发者习惯将方法区称之为“永久代(Parmanent Gen)” ，但严格本质上说两者不同，或者说使用永久代来实现方法区而已，永久代是方法区(相当于是一个接口interface)的一个实现，jdk1.7的版本中，已经将原本放在永久代的字符串常量池移走。

 永久存储区是一个常驻内存区域，用于存放JDK自身所携带的 Class,Interface 的元数据，也就是说它存储的是运行环境必须的类信息，

**被装载进此区域的数据是不会被垃圾回收器回收掉的，关闭 JVM 才会释放此区域所占用的内存**。

# 二、堆参数调优

JDK 1.8之后将最初的永久代取消了，由元空间取代。

![image-20201031162930400](D:\myself\springboot-example\文档\typora\images\jvm06.png)

元空间与永久代之间最大的区别在于：

​	永久带使用的JVM的堆内存，但是java8以后的**元空间并不在虚拟机中而是使用本机物理内存**。

因此，默认情况下，元空间的大小仅受本地内存限制。类的元数据放入 native memory, 字符串池和类的静态变量放入 java 堆中，这样可以加载多少类的元数据就不再由MaxPermSize 控制, 而由系统的实际可用空间来控制。

![image-20201031163011179](D:\myself\springboot-example\文档\typora\images\jvm07.png)

```
public static void main(String[] args){
	long maxMemory = Runtime.getRuntime().maxMemory() ;//返回 Java 虚拟机试图使用的最大内存量。
	long totalMemory = Runtime.getRuntime().totalMemory() ;//返回 Java 虚拟机中的内存总量。
	System.out.println("MAX_MEMORY = " + maxMemory + "（字节）、" + (maxMemory / (double)1024 / 1024) + "MB");
	System.out.println("TOTAL_MEMORY = " + totalMemory + "（字节）、" + (totalMemory / (double)1024 / 1024) + "MB");
}

```

VM参数： -Xms1024m -Xmx1024m -XX:+PrintGCDetails

## GC日志

**Minor GC **

![image-20201031163608279](D:\myself\springboot-example\文档\typora\images\jvm08.png)

**Full GC**



![image-20201031163704775](D:\myself\springboot-example\文档\typora\images\jvm09.png)

# 疑问：具体运行时的变量，方法，常量到底存放在哪？

1.java静态变量存放位置？

```
public statci final Student stu = new Student();
```

stu 在方法区；stu引用的对象在堆

Class对象是存放在堆区的，不是方法区，类的元数据（元数据并不是类的Class对象！Class对象是加载的最终产品，类的方法代码，变量名，方法名，访问权限，返回值等等都是在方法区的）才是存在方法区的

2.堆中不存放基本类型和对象引用，只存放对象本身？

  堆存放对象，对象中有对象变量，

https://m.imooc.com/wenda/detail/496796





















