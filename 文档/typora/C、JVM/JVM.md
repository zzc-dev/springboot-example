```java
javap -v  Test.class // Test的字节码文件 
```

# 一、JVM体系结构概述

![image-20201030083700650](D:\myself\springboot-example\文档\typora\images\jvm01.png)

# 二. ClassLoader

![image-20210328083622036](D:\myself\springboot-example\文档\typora\images\jvm12.png)

## 2.1 linking过程

- **Verify**

  确保class文件的字节流中包含信息符合当前虚拟机的要求，保证被加载类的正确性

  文件格式验证（开头cofe baby）、元数据验证、字节码验证、符号引用验证

- **Prepare**

  为类变量分配内存并并初始化0值，类变量分配在方法区

  不包含final static常量，final在编译时就分配了，准备阶段会显式初始化

  不会为实例变量分配初始化

- **Resolve**

  将常量池中的符号引用转为直接引用的过程

  解析动作主要针对类或接口、字段、类方法、接口方法、方法类型等

负责加载class文件，class文件在文件开头有特定的文件标示，将class文件字节码内容加载到内存中，并将这些内容转换成方法区中的运行时数据结构
​    ClassLoader只负责class文件的加载，至于它是否可以运行，则由Execution Engine决定 

## 2.2 initialzation

 	初始化阶段就是执行类构造器方法 `<clinit>()` 的过程

​	 该方法无需定义，是javac编译器自动收集类中所有的**类变量**的赋值动作和静态代码块中的语句合并而来

​	构造器方法中的指令按语句在源文件中出现的顺序执行。

```java
public class Test{
    static {
        num = 20
    }
    private static int num = 10;
    public static void main(String[] args){
        System.out.print(num); // 输出10
    }
}
```

​	 构造器是虚拟机视角下的 `<init>`

​	 JVM先执行父类的 `<clinit>` ，在执行子类的

​	 类只会被加载一次，因此虚拟机必须保证类的 `<clinit>` 在多线程下被同步加锁

## 2.3 分类

![image-20201030084107145](D:\myself\springboot-example\文档\typora\images\jvm02.png)

​           java 自带的classLoader：

- **Bootstrap ClassLoader**  

​			启动类加载器，C++实现，加载java.* 下的类

​			并不继承自ClassLoader，没有父类加载器

​			加载扩展类加载器和系统类加载器，并指定为他们的父加载器

- **Extension ClassLoader**  

  扩展类加载器，Java实现，加载javax.* 下的类

- **Application ClassLoader**

  系统类加载器，加载当前应用的classpath的所有类

- **用户自定义**
  - 隔离加载类
  - 修改类加载方法
  - 扩展加载源
  - 防止源码泄漏

```java
// 获取启动类加载器加载类的路径
URL[] urls = sun.misc.Launcher.getBootStrapClassPath.getURLs();
for(URL url : urls){
    sout(url.toExternalForm());
}

// 获取扩展类加载器加载类的路径
String[] extDirs = System.getProperty("java.ext.dirs").split(";");

```

## 2.4 双亲委派

当一个类收到了类加载请求，他首先不会尝试自己去加载这个类，而是把这个请求委派给父类去完成，每一个层次类加载器都是如此，因此所有的加载请求都应该传送到启动类加载其中，只有当父类加载器反馈自己无法完成这个请求的时候（在它的加载路径下没有找到所需加载的Class），子类加载器才会尝试自己去加载。

采用双亲委派的一个好处是比如加载位于 rt.jar 包中的类 java.lang.Object，不管是哪个加载器加载这个类，最终都是委托给顶层的启动类加载器进行加载，这样就保证了使用不同的类加载器最终得到的都是同样一个 Object对象。

优点：

​	避免类的重复加载

​	保护程序安全，防止核心API被随意篡改  

## 2.5  沙箱安全机制

## 2.6 其他

​	JVM必须知道一个类是启动类加载器还是用户类加载器加载的。如果是用户类加载器加载的，那么JVM会将这个**类加载的一个引用作为类型信息的一部分保存在方法区**中。当解析一个类到另一个类的引用时，JVM必须保证这两个类的加载器是相同的

​	![image-20210328100532936](D:\myself\springboot-example\文档\typora\images\jvm13.png)

# 三、运行时数据区

![image-20210328101209010](D:\myself\springboot-example\文档\typora\images\jvm14.png)

## 3.1 Program Counter Register

**PC寄存器**

每个线程都有一个程序计数器，是<strong style="color:red">线程私有</strong>的
就是一个指针，指向方法区中的方法字节码（用来存储指向下一条指令的地址,也即将要执行的指令代码），由执行引擎读取下一条指令，是一个非常小的内存空间，几乎可以忽略不记。

如果执行的是一个Native方法，那这个计数器是空的。

用以完成分支、循环、跳转、异常处理、线程恢复等基础功能。

**唯一一个不会发生内存溢出(OutOfMemory=OOM)的区域**

## 3.2 JVM Stack

栈也叫栈内存，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放，**对于栈来说不存在垃圾回收问题**，只要线程一结束该栈就Over，生命周期和线程一致，是线程私有的。8种基本类型的变量+对象的引用变量+实例方法都是在函数的栈内存中分配。

**先入后出**

### 3.2.1 可能出现的异常

- **StackOverflowError**

  栈大小固定，当线程请求分配的栈容量超过虚拟机允许的最大容量是，抛出该异常

- **OutOfMemoryError**

  栈大小动态扩展。在尝试扩展的时候无法申请到足够的内存，或者在创建新的线程是没有足够的内存去创建栈时，抛出该异常

### 3.2.2 参数

> -Xss 1024k  // 栈大小：默认1024k

### 3.2.3 栈帧

- 本地变量（Local Variables）:输入参数和输出参数以及方法内的变量；
- 操作数栈（Operand Stack）:记录出栈、入栈的操作；
- 动态链接（Dynamic Linking）运行时常量池的方法引用
- 方法的返回地址（Return Address）
- 一些附加信息

>java中叫方法，方法在jvm入栈后叫栈帧，场景不同，叫法不同

每个方法执行的同时都会创建一个栈帧，用于存储**局部变量表、操作数栈、动态链接、方法出口**等信息，每一个方法从调用直至执行完毕的过程，就对应着一个栈帧在虚拟机中入栈到出栈的过程。栈的大小和具体JVM的实现有关，通常在256K~756K之间,与等于1Mb左右。

![image-20201031092451168](D:\myself\springboot-example\文档\typora\images\jvm03.png)

栈内存溢出：Exception in thread "main" java.lang.StackOverflowError

#### 3.2.3.1 局部变量表

![image-20210328140731469](D:\myself\springboot-example\文档\typora\images\jvm15.png)

1. 一个数字数组，主要用于**存储方法参数和方法体内的局部变量**：

- 基本数据类型
- 对象引用
- returnAddress 类型

2. **大小是在编译期间确定下来**，保存在方法的Code属性的maximum_local_variables

3. Slot

   局部变量表最基本的存储单元是slot

   32位占1个Slot，64位占2个Slot

4. **一般方法，局部变量表的第一个值就是this的引用，比static 方法多了一个值**

5. 可以重复使用Slot

   ```java
   // 局部变量表长度：3
   // this、a、b|c，b的作用域有限，执行c=3时复用b的Slot
   public void test(){
       int a=1;
       {
           int b=2
       }
       int c = 3;
   }
   ```

   

2. 局部变量表中的变量是重要的垃圾回收根节点

#### 3.2.3.2 操作数栈

> 在方法执行过程中，根据字节码指令，往栈中写入数据或者提取数据，即入栈push、出栈pop

用于保存计算过程的中间结果，同时作为计算过程中变量的临时存储空间

操作数栈的深度也是在编译器就确定好的，Code.max_stack

栈中元素占用空间大小：

​		32bit占一个栈单位深度

​		64bit占两个

**如果被调用的方法带有返回值，其返回值将会被压入当前栈帧的操作数栈中。**	

```java
public void test(){
	int i = test1();  // 对应的字节码aload0：获取上一个栈帧返回的结果，并保存在操作数栈中
	int j = 10
}
```

#### 3.2.3.3 动态链接

​	**帧数据区：动态链接+方法返回地址+附加信息**

每一个栈帧内部都包含一个指向运行时常量池中该栈帧所属方法的引用，即

动态链接的作用：

​	**将符号引用转换为调用方法的直接引用**

```java
public void test(){
    // 9:  invokevirtual #7  #7指向Costanst Pool，为方法的直接引用
	int i = test1();  
	int j = 10
}
```

#### 3.2.3.4 方法返回地址

存放调用该方法的PC寄存器的值

```java
public void test1(){
	test2();
	int i = 0;
}

// 执行test2完成后，返回test1中的PC寄存器的值，让test1接着执行下面的代码
public void test2(){
	return;
}
```

方法结束：

​	正常执行完毕

​	出现未处理的异常，非正常退出：不会给调用者返回任何值

#### 3.2.3.5 附加信息



### 3.2.4 栈顶缓存技术Top Of Stack Cashing

基于栈式架构所使用的0地址指令更加紧凑，但指令更多。

由于操作数时存储在内存中的，因此频繁地执行内存读/写必然会影响执行速度

解决：

​	**栈顶缓存技术：将栈顶元素全部缓存在CPU的寄存器中，以降低对内存的读写次数，提升执行引擎的执行效率**

### 3.2.5 方法调用

#### 3.2.5.1 静态链接与动态链接

​	**静态链接**：

​			当一个字节码文件被装载进JVM内部，如果被调用方法在编译器可知，且运行时不变：调用方法的符号引用转换为直接引用的过程。

​		    对应着**前期绑定**

​	**动态链接**

​			被调用的方法在编译器无法确定，只能在程序运行期间将符号引用转为直接引用

​			对应着**晚期绑定**

#### 3.2.5.2   虚方法和非虚方法

​		方法在编译期确定了具体的直接引用，在运行期不变，这就是非虚方法

​		静态方法、私有方法、构造器、final方法、父类方法都是非虚方法，其他方法为虚方法

![image-20210328170145688](D:\myself\springboot-example\文档\typora\images\jvm16.png)

#### 3.2.5.3 方法重写

![image-20210328171410831](D:\myself\springboot-example\文档\typora\images\jvm17.png)

**虚方法表**

​	在面向对象的编程中，会很频繁的使用动态分派，如果每次动态分派时都要重新在类的方法元数据中搜索合适的目标的话就可能影响到执行效率。

为了提升性能，JVM采用在类的方法区建立一个虚方法表（virtual method table），使用索引表来查找。

​	每个类都有自己的虚方法表，表中存放中各个方法的实际入口

​	**虚方法表在类的linking过程中被创建并开始初始化**，类的变量初始化准备完成后，JVM会把该类的虚方法表也初始化完毕

## 3.3 本地方法栈

**本地接口**的作用是融合不同的编程语言为 Java 所用，它的初衷是融合 C/C++程序，Java 诞生的时候是 C/C++横行的时候，要想立足，必须有调用 C/C++程序，于是就在内存中专门开辟了一块区域处理标记为native的代码，它的具体做法是 **Native Method Stack中登记 native方法，在Execution Engine 执行时加载native libraies。**

 目前该方法使用的越来越少了，除非是与硬件有关的应用，比如通过Java程序驱动打印机或者Java系统管理生产设备，在企业级应用中已经比较少见。因为现在的异构领域间的通信很发达，比如可以使用 Socket通信，也可以使用Web Service等等，不多做介绍。

## 3.4 堆

**堆可以处于物理上不连续的内存空间，但在逻辑上连续的。**

堆中线程私有的缓冲区 TLAB （Thread Local Allocation Buffer）

### 3.4.1 参数

-Xms和-Xmx通常设置一样的值，目的是为了能够**在垃圾回收机制清理完堆区后不需要重新分隔计算堆区的大小，提高性能**

```shell
-Xms 堆的起始内存，等价于 -XX:InitialHeapSize  默认值：电脑内存大小 / 64
-Xmx 堆的最大内存，等价于 -XX:MaxHeapSize      默认值：电脑内存大小 / 4
```

**新生代和老年代比例：**

```shell
-XX:NewRatio=2 默认2，表示新生代:老年代=1:2
-Xmn 新生代的大小

新生代 = Eden + S0 +S1 默认比例：8:1:1
 -XX:SurvivorRatio=8
 但实际上比例并非如此，jvm有个自适应的内存分配策略-XX:-UseAdaptiveSizePolicy,除非显示设置-XX:SurvivorRatio=8，比例才会如我们想的那样
```



**查看参数设置**

```
方式1：
    jps
    jstat -gc 进程PID
方式2：
	-XX:+PrintGCDetails
```

# 四、本地方法接口（Native Interface）

一个Native Method就是一个Java调用非java代码的接口

现状

​	目前该方法的使用越来越少，除非是与硬件有关的应用。

​	Socket通信、RestFul接口





## 2. Execution Engine

**执行引擎负责解释命令，提交操作系统执行。** 







## 5. Method Area

又叫静态区，存放所有的①类（class），②静态变量（static变量），③静态方法，④常量和⑤成员方法(就是普通方法，由访问修饰符，返回值类型，类名和方法体组成)。



## **7.Heap

​    [image-20201031093113119](D:\myself\springboot-example\文档\typora\images\jvm04.png)

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

## 1.java静态变量存放位置？

```
public statci final Student stu = new Student();
```

stu 在方法区；stu引用的对象在堆

Class对象是存放在堆区的，不是方法区，类的元数据（元数据并不是类的Class对象！Class对象是加载的最终产品，类的方法代码，变量名，方法名，访问权限，返回值等等都是在方法区的）才是存在方法区的

2.堆中不存放基本类型和对象引用，只存放对象本身？

  堆存放对象，对象中有对象变量，

https://m.imooc.com/wenda/detail/496796

## 2. 常量池、运行时常量池、字符串常量池

```
public class Review {
    public static void main(String[] args) {
        String info = "hello world";
        int a = 666;
        final int b = 66;
        int c = 36728;
    }
}
```

此代码编译以后，使用**javap -v Review.class**命令查看字节码

```
Classfile /D:/idea_projects/demo/target/classes/com/example/demo/Review.class
  Last modified 2020-11-21; size 561 bytes
  MD5 checksum 09914fecad4a776e7ac86d1a6fd43baa
  Compiled from "Review.java"
public class com.example.demo.Review
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #5.#26         // java/lang/Object."<init>":()V
   #2 = String             #27            // hello world
   #3 = Integer            36728
   #4 = Class              #28            // com/example/demo/Review
   #5 = Class              #29            // java/lang/Object
   #6 = Utf8               <init>
   #7 = Utf8               ()V
   #8 = Utf8               Code
   #9 = Utf8               LineNumberTable
  #10 = Utf8               LocalVariableTable
  #11 = Utf8               this
  #12 = Utf8               Lcom/example/demo/Review;
  #13 = Utf8               main
  #14 = Utf8               ([Ljava/lang/String;)V
  #15 = Utf8               args
  #16 = Utf8               [Ljava/lang/String;
  #17 = Utf8               info
  #18 = Utf8               Ljava/lang/String;
  #19 = Utf8               a
  #20 = Utf8               I
  #21 = Utf8               b
  #22 = Utf8               c
  #23 = Utf8               MethodParameters
  #24 = Utf8               SourceFile
  #25 = Utf8               Review.java
  #26 = NameAndType        #6:#7          // "<init>":()V
  #27 = Utf8               hello world
  #28 = Utf8               com/example/demo/Review
  #29 = Utf8               java/lang/Object
{
  public com.example.demo.Review();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 8: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/example/demo/Review;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=5, args_size=1
         0: ldc           #2                  // String hello world
         2: astore_1
         3: sipush        666
         6: istore_2
         7: bipush        66
         9: istore_3
        10: ldc           #3                  // int 36728
        12: istore        4
        14: return
      LineNumberTable:
        line 10: 0
        line 11: 3
        line 12: 7
        line 13: 10
        line 14: 14
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      15     0  args   [Ljava/lang/String;
            3      12     1  info   Ljava/lang/String;
            7       8     2     a   I
           10       5     3     b   I
           14       1     4     c   I
    MethodParameters:
      Name                           Flags
      args
}
SourceFile: "Review.java"
```

从字节码文件中可以看到**Constant Pool**。**也就是说，每个class文件，都有一个常量池**

第一个疑问：为什么都是int类型的数据，用的指令却不同？按照JVM的规范，根据int值范围采用不同的指令将int数值入栈。 

 第二个疑问：为什么666和66没有被放到常量池中？据了解，对于int类型，只有超过一定范围的int值，才会放到常量池中， 这也就解释了36728为何被放到了常量池中。

### 2.2 常量池

<img src="D:\myself\springboot-example\文档\typora\images\jvm10" alt="img" style="zoom:80%;" />



### 2.3 运行时常量池

在常量池中，可以看到都是用#1 #2 #3这些临时符号来表示。

当运行某个程序时候，JVM会把所有的字节码文件加入到内存当中，在经过链接、验证后，将#1 #2 #3这些符号全部转换成内存中的实际地址，放入到运行时常量池运行。

**运行时常量池是放在方法区中的，全局只有一份，是一个被所有的class共享的区域**。

### 2.4 字符串常量池

```java
String a = "test1";
String b = "test2";
```

这两个字符串"test1"和"test2"在编译完成后，首先存放在常量池中。

在程序运行时，加载进入内存以后，字符串就会加载进入到字符串常量池中。jdk1.7以后，字符串常量池位于堆中。

## 3.栈帧

1. 局部变量表

   方法参数和方法体中的局部变量。在方法执行过程中，jvm使用局部变量表完成参数值到参数变量列表的传递过程

2. 操作数栈

3. 动态链接

   每个栈帧都有一个指向运行时常量池中该栈帧所属方法的引用。持有这个引用是为了支持方法调用过程中的动态连接。

   Class文件的常量池存有大量的符号引用，字节码中的方法调用指令就以常量池中指向方法的符号引用作为参数。
   这些符号引用一部分在类加载阶段或者第一次使用时转化为直接引用，这种转化称为**静态解析**。
   另外一部分将在每一次运行时转化为直接引用，这部分称为**动态连接**

4. 方法返回地址

5. 附加信息

   虚拟机规范允许具体的虚拟机实现增加一些规范中没有描述的信息到栈帧中，例如与调式有关的信息。这部分信息取决于虚拟机的实现。











