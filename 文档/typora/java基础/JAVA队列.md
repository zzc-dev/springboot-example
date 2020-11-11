#  ConcurrentLinkedQueue

https://blog.csdn.net/qq_38293564/article/details/80798310

![img](D:\myself\springboot-example\文档\typora\images\queue05.png)

## 1. 简介

ConcurrentLinkedQueue**是一个基于链接节点的无界线程安全队列**，它采用先进先出的规则对节点进行排序，当我们添加一个元素的时候，它会添加到队列的尾部，当我们获取一个元素时，它会返回队列头部的元素。它采用了“wait－free”算法来实现，该算法在Michael & Scott算法上进行了一些修改。

- 使用 CAS 原子指令来处理对数据的并发访问，这是非阻塞算法得以实现的基础。
- head/tail 并非总是指向队列的头 / 尾节点，也就是说允许队列处于不一致状态。 这个特性把入队 / 出队时，原本需要一起原子化执行的两个步骤分离开来，从而缩小了入队 / 出队时需要原子化更新值的范围到唯一变量。这是非阻塞算法得以实现的关键。
- 由于队列有时会处于不一致状态。为此，ConcurrentLinkedQueue 使用[三个不变式](https://www.ibm.com/developerworks/cn/java/j-lo-concurrent/index.html)来维护非阻塞算法的正确性。
- 以批处理方式来更新 head/tail，从整体上减少入队 / 出队操作的开销。
- 为了有利于垃圾收集，队列使用特有的 head 更新机制；为了确保从已删除节点向后遍历，可到达所有的非删除节点，队列使用了特有的向后推进策略