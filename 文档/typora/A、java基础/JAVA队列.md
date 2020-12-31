# 一、Queue接口

api   https://blog.csdn.net/a724888/article/details/80275501

```
add/offer      队尾添加          超过队列长度：抛异常/返回false
remove/poll    获取对头数据并移除  队列无数据：抛异常/返回null
element/peek   获取对头数据       队列无数据：抛异常/返回null
```

# 二、BlockingQueue

```
interface BlockingQueue<E> extends Queue<E>
```

|      | 抛出异常                                                     | 特殊值                                                       | 阻塞                                                         | 超时                                                         |
| ---- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 插入 | [`add(e)`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`offer(e)`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`put(e)`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`offer(e, time, unit)`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) |
| 移除 | [`remove()`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`poll()`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`take()`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`poll(time, unit)`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) |
| 检查 | [`element()`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | [`peek()`](https://blog.csdn.net/wei_ya_wen/article/details/19344939) | 不可用                                                       | 不可用                                                       |

## 2.1 LinkedBlockingQueue

### 2.1.1 构造器与Node

默认构造器是无界参数

存储方式：单向链表Node

```
public LinkedBlockingQueue() {
     this(Integer.MAX_VALUE);
}
public LinkedBlockingQueue(int capacity) {
    if (capacity <= 0) throw new IllegalArgumentException();
    this.capacity = capacity;
    last = head = new Node<E>(null);
}
```

```
static class Node<E> {
    E item;
    Node<E> next;
    Node(E x) { item = x; }
}
```

## 2.2 add/offer

```
public boolean add(E e) {
    if (offer(e))
        return true;
    else
        throw new IllegalStateException("Queue full");
}

public boolean offer(E e) {
    if (e == null) throw new NullPointerException();
    final AtomicInteger count = this.count;
    if (count.get() == capacity)
        return false;
    int c = -1;
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    putLock.lock();
    try {
        if (count.get() < capacity) {
            enqueue(node);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        }
    } finally {
        putLock.unlock();
    }
    if (c == 0)
        signalNotEmpty();
    return c >= 0;
}
```

```
private void enqueue(Node<E> node) {
    // assert putLock.isHeldByCurrentThread();
    // assert last.next == null;
    last = last.next = node;
}
```

<img src="D:\myself\springboot-example\文档\typora\images\queue07.png" alt="在这里插入图片描述" style="zoom:50%;" />

## 2. remove

```
private E dequeue() {
    // assert takeLock.isHeldByCurrentThread();
    // assert head.item == null;
    Node<E> h = head;
    Node<E> first = h.next;
    h.next = h; // help GC
    head = first;
    E x = first.item;
    first.item = null;
    return x;
}
```

head的item永远为null    null -> A->B->null

## 3. 总结

- 队列大小有所不同，ArrayBlockingQueue是**有界**的初始化必须指定大小，而LinkedBlockingQueue可以是有界的也可以是无界的(Integer.MAX_VALUE)，对于后者而言，当添加速度大于移除速度时，在无界的情况下，可能会造成内存溢出等问题。
- 数据存储容器不同，ArrayBlockingQueue采用的是数组作为数据存储容器，而LinkedBlockingQueue采用的则是以Node节点作为连接对象的链表。
- 由于ArrayBlockingQueue采用的是数组的存储容器，因此在插入或删除元素时不会产生或销毁任何额外的对象实例，而LinkedBlockingQueue则会生成一个额外的Node对象。这可能在长时间内需要高效并发地处理大批量数据的时，对于GC可能存在较大影响。
- 两者的实现队列添加或移除的锁不一样，ArrayBlockingQueue实现的队列中的锁是没有分离的，即添加操作和移除操作采用的同一个ReenterLock锁，而LinkedBlockingQueue实现的队列中的锁是分离的，其添加采用的是putLock，移除采用的则是takeLock，这样能大大提高队列的吞吐量，也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列的并发性能。

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



#  四、ConcurrentLinkedQueue

https://blog.csdn.net/qq_38293564/article/details/80798310

![img](D:\myself\springboot-example\文档\typora\images\queue05.png)

## 1. 简介

ConcurrentLinkedQueue**是一个基于链接节点的无界线程安全队列**，它采用先进先出的规则对节点进行排序，当我们添加一个元素的时候，它会添加到队列的尾部，当我们获取一个元素时，它会返回队列头部的元素。它采用了“wait－free”算法来实现，该算法在Michael & Scott算法上进行了一些修改。

- 使用 CAS 原子指令来处理对数据的并发访问，这是非阻塞算法得以实现的基础。
- head/tail 并非总是指向队列的头 / 尾节点，也就是说允许队列处于不一致状态。 这个特性把入队 / 出队时，原本需要一起原子化执行的两个步骤分离开来，从而缩小了入队 / 出队时需要原子化更新值的范围到唯一变量。这是非阻塞算法得以实现的关键。
- 由于队列有时会处于不一致状态。为此，ConcurrentLinkedQueue 使用[三个不变式](https://www.ibm.com/developerworks/cn/java/j-lo-concurrent/index.html)来维护非阻塞算法的正确性。
- 以批处理方式来更新 head/tail，从整体上减少入队 / 出队操作的开销。
- 为了有利于垃圾收集，队列使用特有的 head 更新机制；为了确保从已删除节点向后遍历，可到达所有的非删除节点，队列使用了特有的向后推进策略

# 五、阻塞队列之ArrayBlockingQueue

## 1. 初始化

内部使用数组存储数据（**有界队列**），初始化时必须指定数组长度，使用了ReentrantLock实现了线程安全，默认非公平锁

```
public ArrayBlockingQueue(int capacity, boolean fair) {
    if (capacity <= 0)
        throw new IllegalArgumentException();
    this.items = new Object[capacity];
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}
```

## 2. 入队出队

![img](D:\myself\springboot-example\文档\typora\images\queue06.png)



入队出队都使用了ReentrantLock保证了数组的线程安全。

依靠putIndex和takeIndex区别尾部和头部

- 

# 七、PriorityBlockingQueue

PriorityBlockingQueue是一个支持优先级的**无界阻塞队列**，直到系统资源耗尽。默认情况下元素采用自然顺序升序排列。也可以自定义类实现compareTo()方法来指定元素排序规则，或者初始化PriorityBlockingQueue时，指定构造参数Comparator来对元素进行排序。但需要注意的是不能保证同优先级元素的顺序。\

PriorityBlockingQueue也是基于最小二叉堆实现，**使用基于CAS实现的自旋锁来控制队列的动态扩容，保证了扩容操作不会阻塞take操作的执行**。

线程安全：**放，取，移除 的时候都加锁ReentrantLock**



