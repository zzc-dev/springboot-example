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