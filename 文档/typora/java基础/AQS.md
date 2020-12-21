# AbstractQueuedSynchronizer

https://javadoop.com/post/AbstractQueuedSynchronizer#toc0

https://javadoop.com/post/AbstractQueuedSynchronizer-2

https://coderbee.net/index.php/concurrent/20131115/577 【**自旋锁、排队自旋锁、MCS锁、CLH锁**】

# 一、框架

AQS是一个通过内置的**FIFO**双向队列来完成线程的排队工作(内部通过结点head和tail记录队首和队尾元素，元素的结点类型为Node类型

<img src="D:\myself\springboot-example\文档\typora\images\aqs01.png" alt="img" style="zoom: 80%;" />

## 1.1 AbstractQueuedSynchronizer

state:  0  未锁定状态
state:  N  锁定状态，需要释放N次锁使得state=0，这就是可重入锁，线程可以重复获取同一个锁《》

**<strong style="color:red">head: 要么是初始化节点，thread=null，要么是拥有锁的节点（正在执行的线程）</strong>**

自定义同步器需实现以下几种方法：

- isHeldExclusively()：该线程是否正在独占资源。只有用到condition才需要去实现它
- tryAcquire(int): 独占方式，成功返回true，失败返回false
- tryRelease(int): 独占方式，成功返回true，失败返回false
- tryAcquireShared(int):共享方式。尝试获取资源，<0 获取失败；=0获取成功但没有剩余可用资源；>0获取成功且有可用资源
- tryAcquireRelease(int):共享方式。释放后允许唤醒后续等待节点返回true，否则返回false

ReentrantLock，state初始化为0，表示未锁定状态。A线程lock()时，会调用tryAcquire()独占该锁并将state+1。此后，其他线程再tryAcquire()时就会失败，直到A线程unlock()到state=0（即释放锁）为止，其它线程才有机会获取该锁。当然，释放锁之前，A线程自己是可以重复获取此锁的（state会累加），这就是可重入的概念。但要注意，获取多少次就要释放多么次，这样才能保证state是能回到零态的

CountDownLatch，任务分为N个子线程去执行，state也初始化为N（注意N要与线程个数一致）。这N个子线程是并行执行的，每个子线程执行完后countDown()一次，state会CAS减1。等到所有子线程都执行完后(即state=0)，会unpark()主调用线程，然后主调用线程就会从await()函数返回，继续后余动作

## 1.2 Node

`thread` 用来存放进入AQS队列中的线程引用;
`SHARED` 表示标记线程是因为获取共享资源失败被阻塞添加到队列中的；
`EXCLUSIVE` 表示线程因为获取独占资源失败被阻塞添加到队列中的;
`waitStatus` 表示当前线程的等待状态:
        ① **CANCELLED=1** 线程中断或者等待超时，需要从等待队列中移除
		② **SIGNAL=-1**  后继节点需要被唤醒
        ③ **CONDITION=-2**：表示结点在等待队列中(这里指的是等待在某个lock的condition上，关于Condition的原理下面会写到)，当持有锁的线程调用了Condition的signal()方法之后，结点会从该**condition的等待队列转移到该lock的同步队列**上，去竞争lock。(注意：这里的同步队列就是我们说的AQS维护的FIFO队列，等待队列则是每个condition关联的队列)  
		④ **PROPAGTE=-3**：表示下一次共享状态获取将会传递给后继结点获取这个共享同步状态
        ⑤ **0**：新结点入队时的默认状态
   **负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常**

# 二、acquire(int)

获取独占资源，也就是lock的语义

```
 public final void acquire(int arg) {
     if (!tryAcquire(arg) &&
         acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
         selfInterrupt();
 }
```

　函数流程如下：

1. 1. tryAcquire()尝试直接去获取资源，如果成功则直接返回（这里体现了非公平锁，每个线程获取锁时会尝试直接抢占加塞一次，而CLH队列中可能还有别的线程在等待）；
   2. addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；
   3. acquireQueued()使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
   4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。

## 2.1 tryAcquire(arg)

   获取到锁返回true，否则fasle，获取资源交给子类自己实现

```
protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
}
```

## 2.2 addWaiter

 将节点设为独占类型，加入到队尾

enq入队是通过**CAS自旋volatile变量**

```
 private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        //尝试快速方式直接放到队尾。
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        //上一步失败则通过enq入队。
        enq(node);
        return node;
    }
```

## 2.3 acquireQueued(node)

该线程获取资源失败，已经被放入等待队列尾部；**进入等待状态休息，直到其他线程彻底释放资源后唤醒自己**。

看完 `shouldParkAfterFailedAcquire `和 `parkAndCheckInterrupt` 总结：

 	1.   拿到前驱节点p，如果p是头节点，则尝试获取锁，如果拿到，返回中断状态，否则继续2
 	2.   检查当前节点是否可以进入休眠状态，是则3，否则自旋执行1
 	3.   调用park()进入wating状态，直到被unpark或interrupt唤醒自己，返回是否被中断，然后继续流程1

```
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;  // 标记是否成功拿到资源
        try {
            boolean interrupted = false; // 标记等待过程中是否被中断过
            for (;;) {
                final Node p = node.predecessor();  // 拿到前驱节点
                if (p == head && tryAcquire(arg)) { // 如果前驱节点是头节点，代表当前节点可以尝试获取资源
                    setHead(node);  // 拿到资源后，将当前节点设为头节点，
                    p.next = null; // help GC 回收以前的头节点，之前拿完资源的节点出队了
                    failed = false; // 成功拿到资源
                    return interrupted; // 返回中断状态
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally { // 如果等待过程中没有成功获取资源（如timeout，或者可中断的情况下被中断了），那么取消结点在队列中的等待。
            if (failed)
                cancelAcquire(node);
        }
    }
```

### 2.3.1 shouldParkAfterFailedAcquire

> node入队时的状态waitStatus=0，
>  waitStatus 中 SIGNAL(-1) 状态的意思，Doug Lea 注释的是：代表后继节点需要被唤醒。也就是说这个 waitStatus 其实代表的不是自己的状态，而是后继节点的状态，我们知道，每个 node 在入队的时候，都会把前驱节点的状态改为 SIGNAL，然后阻塞，等待被前驱唤醒。

用于查询前驱节点的状态，看看自身是否可以去休息：

​	1.前驱节点 = SINGNAL  前驱节点拿到资源可以运行并且执行完后会通知自己，可以休息了
​    2.前驱节点 > 0 代表该前驱节点已经被放弃了，找到一个正常等待的节点，把当前节点放在他的尾部
​    3.前驱节点<0 && !=SINGNAL  前驱正常，那就把前驱的状态设置成SIGNAL，告诉它拿完号后通知自己一下

```
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;//拿到前驱的状态
    if (ws == Node.SIGNAL)
        //如果已经告诉前驱拿完号后通知自己一下，那就可以安心休息了
        return true;
    if (ws > 0) {
        /*
         * 如果前驱放弃了，那就一直往前找，直到找到最近一个正常等待的状态，并排在它的后边。
         * 注意：那些放弃的结点，由于被自己“加塞”到它们前边，它们相当于形成一个无引用链，稍后就会被保安大叔赶走了(GC回收)！
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
         //如果前驱正常，那就把前驱的状态设置成SIGNAL，告诉它拿完号后通知自己一下。有可能失败，人家说不定刚刚释放完呢！
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

### 2.3.2 parkAndCheckInterrupt

调用park方法，是当前线程进入waiting状态，直到被其他线程唤醒：1）被unpark  2）被中断
返回中断状态

```
1 private final boolean parkAndCheckInterrupt() {
2     LockSupport.park(this);//调用park()使线程进入waiting状态
3     return Thread.interrupted();//如果被唤醒，查看自己是不是被中断的。
4 }
```

## 2.4 小结acquire

```
public final void acquire(int arg) {
     if (!tryAcquire(arg) &&
         acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
         selfInterrupt();
 }
```

1. 调用自定义同步器的tryAcquire()尝试直接去获取资源，如果成功则直接返回；

2. 没成功，则addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；

3. acquireQueued()使线程在等待队列中休息，有机会时（轮到自己，会被unpark()）会去尝试获取资源。获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。

4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。

   ![img](D:\myself\springboot-example\文档\typora\images\aqs02.png)

# 三、release(int)

**用unpark()唤醒等待队列中最前边的那个未放弃线程**

```
public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
   }
```

## 3.1 unparkSuccessor

```
private void unparkSuccessor(Node node) {
    //这里，node一般为当前线程所在的结点。
    int ws = node.waitStatus;
    if (ws < 0)//置零当前线程所在的结点状态，允许失败。
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;//找到下一个需要唤醒的结点s
    if (s == null || s.waitStatus > 0) {//如果为空或已取消
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev) // 从后向前找。
            if (t.waitStatus <= 0)//从这里可以看出，<=0的结点，都是还有效的结点。
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);//唤醒
}
```

# 四、acquireShared(int)

尝试获取锁，获取失败则加入等待队列

```
1 public final void acquireShared(int arg) {
2     if (tryAcquireShared(arg) < 0)
3         doAcquireShared(arg);
4 }
```

## 4.1 doAcquireShared(int)

```
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);//加入队列尾部
    boolean failed = true;//是否成功标志
    try {
        boolean interrupted = false;//等待过程中是否被中断过的标志
        for (;;) {
            final Node p = node.predecessor();//前驱
            if (p == head) {//如果到head的下一个，因为head是拿到资源的线程，此时node被唤醒，很可能是head用完资源来唤醒自己的
                int r = tryAcquireShared(arg);//尝试获取资源
                if (r >= 0) {//成功
                    setHeadAndPropagate(node, r);//将head指向自己，还有剩余资源可以再唤醒之后的线程
                    p.next = null; // help GC
                    if (interrupted)//如果等待过程中被打断过，此时将中断补上。
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }

            //判断状态，寻找安全点，进入waiting状态，等着被unpark()或interrupt()
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

### 4.1.1 setHeadAndPropagate

```
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head;
    setHead(node);//head指向自己
     //如果还有剩余量，继续唤醒下一个邻居线程
    if (propagate > 0 || h == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

# 五、releaseShared

```
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {//尝试释放资源
        doReleaseShared();//唤醒后继结点
        return true;
    }
    return false;
}
```

## 5.1 doReleaseShared

```
private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;
                unparkSuccessor(h);//唤醒后继
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;
        }
        if (h == head)// head发生变化
            break;
    }
}
```

# 六、公平锁与非公平锁

公平锁：首先看是否有线程再等待，有则直接加入到等待队列，没有尝试获取锁

非公平锁：1.调用lock后，第一次调用CAS进行一次抢锁，如果成功，获取锁返回
                   2.第一次失败后，调用tryAcquire()不会像公平锁那样判断是否有线程等待，第二次调用CAS再进行抢锁

公平锁和非公平锁就这两点区别，如果这两次 CAS 都不成功，那么后面非公平锁和公平锁是一样的，都要进入到阻塞队列等待唤醒。

相对来说，非公平锁会有更好的性能，因为它的吞吐量比较大。当然，非公平锁让获取锁的时间变得更加不确定，可能会导致在阻塞队列中的线程长期处于饥饿状态。

# 七、Condition

## 7.1 介绍

Condition 的实现类 `AbstractQueuedSynchronizer` 类中的 `ConditionObject`

```
public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        // 条件队列的第一个节点
          // 不要管这里的关键字 transient，是不参与序列化的意思
        private transient Node firstWaiter;
        // 条件队列的最后一个节点
        private transient Node lastWaiter;
        ......
```

在前面介绍 AQS 的时候，我们有一个**阻塞队列**，用于保存等待获取锁的线程的队列。这里我们引入另一个概念，叫**条件队列**

<img src="D:\myself\springboot-example\文档\typora\images\aqs03.png" alt="condition-2" style="zoom: 50%;" />

## 7.2 await

```
// 首先，这个方法是可被中断的，不可被中断的是另一个方法 awaitUninterruptibly()
// 这个方法会阻塞，直到调用 signal 方法（指 signal() 和 signalAll()，下同），或被中断
public final void await() throws InterruptedException {
    // 老规矩，既然该方法要响应中断，那么在最开始就判断中断状态
    if (Thread.interrupted())
        throw new InterruptedException();

    // 添加到 condition 的条件队列中
    Node node = addConditionWaiter();

    // 释放锁，返回值是释放锁之前的 state 值
    // await() 之前，当前线程是必须持有锁的，这里肯定要释放掉
    int savedState = fullyRelease(node);

    int interruptMode = 0;
    // 这里退出循环有两种情况，之后再仔细分析
    // 1. isOnSyncQueue(node) 返回 true，即当前 node 已经转移到阻塞队列了
    // 2. checkInterruptWhileWaiting(node) != 0 会到 break，然后退出循环，代表的是线程中断
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    // 被唤醒后，将进入阻塞队列，等待获取锁
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```

### 7.2.1 将节点加入条件队列 addConditionWaiter

```
// 将当前线程对应的节点入队，插入队尾
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // 如果条件队列的最后一个节点取消了，将其清除出去
    // 为什么这里把 waitStatus 不等于 Node.CONDITION，就判定为该节点发生了取消排队？
    if (t != null && t.waitStatus != Node.CONDITION) {
        // 这个方法会遍历整个条件队列，然后会将已取消的所有节点清除出队列
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    // node 在初始化的时候，指定 waitStatus 为 Node.CONDITION
    Node node = new Node(Thread.currentThread(), Node.CONDITION);

    // t 此时是 lastWaiter，队尾
    // 如果队列为空
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
```

#### 7.2.1.1 遍历链表将已经取消等待的节点清除 unlinkCancelledWaiters

```
// 等待队列是一个单向链表，遍历链表将已经取消等待的节点清除出去
// 纯属链表操作，很好理解，看不懂多看几遍就可以了
private void unlinkCancelledWaiters() {
    Node t = firstWaiter;
    Node trail = null;
    while (t != null) {
        Node next = t.nextWaiter;
        // 如果节点的状态不是 Node.CONDITION 的话，这个节点就是被取消的
        if (t.waitStatus != Node.CONDITION) {
            t.nextWaiter = null;
            if (trail == null)
                firstWaiter = next;
            else
                trail.nextWaiter = next;
            if (next == null)
                lastWaiter = trail;
        }
        else
            trail = t;
        t = next;
    }
}
```

### 7.2.2 完全释放独占锁 fullyRelease

> 考虑一下，如果一个线程在不持有 lock 的基础上，就去调用 condition1.await() 方法，它能进入条件队列，但是在上面的这个方法中，由于它不持有锁，release(savedState) 这个方法肯定要返回 false，进入到异常分支，然后进入 finally 块设置 `node.waitStatus = Node.CANCELLED`，这个已经入队的节点之后会被后继的节点”请出去“。

```
// 首先，我们要先观察到返回值 savedState 代表 release 之前的 state 值
// 对于最简单的操作：先 lock.lock()，然后 condition1.await()。
//         那么 state 经过这个方法由 1 变为 0，锁释放，此方法返回 1
//         相应的，如果 lock 重入了 n 次，savedState == n
// 如果这个方法失败，会将节点设置为"取消"状态，并抛出异常 IllegalMonitorStateException
final int fullyRelease(Node node) {
    boolean failed = true;
    try {
        int savedState = getState();
        // 这里使用了当前的 state 作为 release 的参数，也就是完全释放掉锁，将 state 置为 0
        if (release(savedState)) {
            failed = false;
            return savedState;
        } else {
            throw new IllegalMonitorStateException();
        }
    } finally {
        if (failed)
            node.waitStatus = Node.CANCELLED;
    }
}
```



























