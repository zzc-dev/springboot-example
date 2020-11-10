# 一、Queue

api   https://blog.csdn.net/a724888/article/details/80275501

```
add/offer      队尾添加          超过队列长度：抛异常/返回false
remove/poll    获取对头数据并移除  队列无数据：抛异常/返回null
element/peek   获取对头数据       队列无数据：抛异常/返回null
```

# 二、Deque

> DeQueue(Double-ended queue)为接口，继承了Queue接口，创建双向队列，灵活性更强，可以前向或后向迭代，在队头队尾均可心插入或删除元素。它的两个主要实现类是ArrayDeque和LinkedList。

## 1. ArrayDeque

底层使用循环数组实现双向队列

# 三、 PriorityQueue

> 数组实现堆的结构

PriorityQueue是基于优先堆的一个无界队列，这个优先队列中的元素可以默认自然排序或者通过提供的[Comparator](http://www.journaldev.com/780/java-comparable-and-comparator-example-to-sort-objects)（比较器）在队列实例化的时排序

**优先队列的大小是不受限制的**，但在创建时可以指定初始大小。当我们向优先队列增加元素的时候，队列大小会自动增加。

PriorityQueue是非线程安全的，所以Java提供了PriorityBlockingQueue（实现[BlockingQueue接口](http://www.journaldev.com/1034/java-blockingqueue-example-implementing-producer-consumer-problem)）用于[Java多线程环境](http://www.journaldev.com/1079/java-thread-tutorial)。

优先队列不允许空值，而且不支持non-comparable（不可比较）的对象，比如用户自定义的类。优先队列要求使用[Java Comparable和Comparator接口](http://www.journaldev.com/780/java-comparable-and-comparator-example-to-sort-objects)给对象排序，并且在排序时会按照优先级处理其中的元素。

优先队列的头是基于自然排序或者[Comparator](http://www.journaldev.com/780/java-comparable-and-comparator-example-to-sort-objects)排序的最小元素。如果有多个对象拥有同样的排序，那么就可能随机地取其中任意一个。当我们获取队列时，返回队列的头对象。



```
transient Object[] queue;  数组的下标对应着完全二叉树的位置
```

<img src="D:\myself\springboot-example\文档\typora\images\queue01.png" alt="PriorityQueue_base.png" style="zoom:50%;" />

## 1. offer

每次添加数据首先添加到队尾，然后和他的父节点比较，如果newNode<parentNode（优先级高），则将newNode和parentNode互换位置，

继续和父级比较，直到newNode>=parentNode或者到了根节点。

**数组中的元素并不是按优先级排序的：**

>任一一个节点，他的左右两个节点一定比他优先级低（根节点优先级最高！）
>
>不能保证，左节点的优先级比右节点高。



<img src="D:\myself\springboot-example\文档\typora\images\queue02.png" alt="PriorityQueue_offer.png" style="zoom:50%;" />

## 2. poll

经过poll后，根节点被移除，一颗树变成了两颗完全二叉树，需要进行必要的调整：

**从`k`指定的位置开始，将`x`逐层向下与当前点的左右孩子中较小的那个交换，直到`x`小于或等于左右孩子中的任何一个为止**。

例如 ： k=0， x=9 一般是从第1层比较，比较的值是数组的最后一个值



<img src="D:\myself\springboot-example\文档\typora\images\queue03.png" alt="PriorityQueue_poll.png" style="zoom:50%;" />

