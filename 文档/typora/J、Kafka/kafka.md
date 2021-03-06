# 一、概述

>Kafka是一个**分布式**的基于**发布/订阅**模式的消息队列，主要应用于大数据实时处理领域

Kafka 是linkedin 公司用于日志处理的分布式消息队列，同时支持离线和在线日志处理。kafka 对消息保存时根据Topic进行归类，发送消息者成为Producer,消息接受者成为Consumer,此外kafka 集群有多个kafka 实例组成，每个实例(server)称为broker。无论是kafka集群，还是producer和consumer 都依赖于zookeeper 来保证系统可用性，为集群保存一些meta 信息。

## 1.1 消息队列的两种模式

**1. 点对点模式**

​	一对一，消费者主动拉取数据，消息收到后消息清除
​    消息被消费以后，queue中不再有存储，所以消费者不可能消费到已经被消费的消息。Queue支持存在多个消费者，但是对一个消息而言，只会有一个消费者可以消费

**2. 发布订阅模式**

  一对多，并且有两种消费模式，一是队列主动推送，二是消费者基于长连接轮询主动拉取数据，kafka属于后者

## 1.2 优点：

>分布式
>
><strong style="color:red">生产者写入数据采用：顺序写入和页缓存技术 </strong>
>
><strong style="color:red">消费者读取数据采用：零拷贝技术 </strong>

- 首先说一下，为什么顺序写入比随机写入快：先介绍一下它的存储原理。机械硬盘的结构你可以想象成一个唱片机，它有一个旋转的盘片和一个能沿半径方向移动的磁头。处理读取和写入请求时，首先可以根据请求的开始地址算出要处理的数据在磁盘上的位置，之后要进行以下几步工作：1、磁头沿半径方向移动，直至移动到数据所在的柱面（相同半径的磁道组成的环面）2、盘片高速旋转，使磁头到达数据的起始位置3、磁头沿磁道从磁盘读取或写入数据。当一次读取的数据量很少的时候，1、2步骤带来的开销是无法忽略的，这使得随机写相对于顺序写会有巨大的性能劣势。因为在顺序写的时候，1、2步骤只需要执行一次，剩下的全是数据传输所需要的固有开销；而每次随机写的时候，前两个步骤都需要执行，带来了极大的额外开销。
- 其次再说一下**页缓存**：即便是顺序写入硬盘，硬盘的访问速度还是不可能追上内存。所以Kafka的数据并 不是实时的写入硬盘 ，它充分利用了现代操作系统 分页存储 来利用内存提高I/O效率。Memory Mapped Files也被翻译成 内存映射文件 ，在64位操作系统中一般可以表示20G的数据文件，它的工作原理是直接利用操作系统的Page来实现文件到物理内存的直接映射。完成映射之后你对**物理内存的操作会被同步到硬盘上**(操作系统在适当的时候)，也就是说，将**复杂的IO操作交给了操作系统**。

高吞吐、低延时：kafka每秒可以处理几十万条消息，它的延迟最低只有几毫秒；

持久化、可靠性、容错性

高并发：单机可支持数千个客户端同时读写；

可扩展：kafka集群支持热扩展；

解耦，消峰，解决生产者速度>消费者速度的问题

## 1.2 kafka 文件存储机制

![image-20210201211711400](D:\myself\springboot-example\文档\typora\images\kafka04.png)

kafka中消息是以topic进行分类的，生产者生产消息，消费组消费消息，都是面向topic的

topic是逻辑概念，而partition是物理概念，每个partition对应一个log文件，该log文件存储生产者生产的数据，不断追加到该文件的尾端，且每条数据都有自己的offset。消费者组中的每个消费者，都会实时记录自身的offset，以便出错恢复时，从上次的位置继续消费。

![image-20210201212211947](D:\myself\springboot-example\文档\typora\images\kafka05.png)

每个分区目录下都有.index和.log文件，每个log文件存储的大小可配置，默认1G。分区采用分段机制存储，当超过1G时，再次创建一个log文件，文件名为自身最小的offset，当消费组消费消息时，首先根据自身的offset找到index对应的log文件偏移量，根据文件偏移量采用**二分查找**找到对应的log文件。

# 二、生产者

## 2.1 分区

   消费者默认50个分区consumer-offset-n

### 2.1.1 分区原因：

1. 方便在集群中扩展。每个分区可以通过调整以适应所在机器的存储大小，处理速度，而一个topic是由多个partition组成，这样整个集群就可以适应任意大小的数据了（l对broke数据分片，通过减少消息容量来提升IO性能）

 	2. 提高并发度。

### 2.1.2 分区策略（生产者）

​		生产者将消息封装成一个`ProducerRecord`对象

![image-20210202110638824](D:\myself\springboot-example\文档\typora\images\kafka06.png)

1. 指定partition
2. 没有指明partition但有key的情况，将key的hash值与分区数取模后得到partition值
3. partition和key都没有指定，第一次调用时随机生成一个值然后取模得到partition，之后将这个值自增。这就是`round-robin`(轮询)算法

## 2.2 数据可靠性

**ack机制**确保数据成功发送到指定的topic，否则重发

![image-20210202111403037](D:\myself\springboot-example\文档\typora\images\kafka07.png)



### 2.2.1 副本同步策略

| **方案**                    | **优点**                                           | **缺点**                                             |
| --------------------------- | -------------------------------------------------- | ---------------------------------------------------- |
| 半数以上完成同步，就发送ack | 延 迟 低                                           | 选举新的leader时，容忍n台节点的故障，需要 2n+1个副本 |
| 全部完成同步，才发送ack     | 选举新的leader时，容忍n台节点的故障，需要n+1个副本 | 延迟高                                               |

kafka选择第二种方案，原因：

	1. 同样为了容忍n台节点的故障，第一种需要2n+1个节点确保半数以上原则，kafka每个分区都有大量数据，会造成大量数据的冗余
	2. 虽然第二种方案的网络延迟会比较高，但网络延迟对Kafka的影响较小。
但第二种方案也有不足，如果有一个节点故障，那同步永远完成不了，kafka也无法发送ack，为了应对这个问题，kafka采取`ISR`机制

### 2.2.2 ISR

Leader维护了一个动态的`in-sync replica set `(ISR)，意为和leader保持同步的follower集合。

当ISR中的follower完成数据的同步之后，leader就会给follower发送ack。如果follower长时间未向leader同步数据，则该follower将被踢出ISR，该时间阈值由**replica.lag.time.max.ms**参数设定。Leader发生故障之后，就会从ISR中选举新的leader。

ISR存在leader和zookeepr中，是一个动态的队列，当超时（**replica.lag.time.max.ms**）或者主从分片同步数量超出阈值（**replica.lag.max.message**），ISR移除follower，之后又满足条件又加入了isr中。

在0.9版本后，移除了**replica.lag.max.message**配置，原因：当producer批量生产数量>**replica.lag.max.message**时，一定会导致ISR移除所有的follower，直到满足又加入了ISR，频繁的对ISR队列操作，会影响kafka性能。

### 2.2.3 ACK机制

对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，没必要等ISR中的follower全部接收成功。

参数配置

 1. acks=0

    producer不等待broker的ack，当broker故障时有可能**丢失数据**

 2. acks=1

    producer等待broker的ack，只要leader落盘成功就返回ack。如果follower同步之前leader故障，那么将会**丢失数据**

 3. acks=-1（all）

    producer等待broker的ack，必须等待leader和follower全部落盘成功才返回ack。如果在同步follower后，leader发送ack前，leader故障，会造成**数据重复**

    **数据重复**： 此时follower节点网络故障，没有在`isr`中，此时`isr`只有一个leader，当leader落盘并返回`ack`后，发生故障。这是极限情况，一般不会发生

### 2.2.4 HW和LEO

![image-20210202221238829](D:\myself\springboot-example\文档\typora\images\kafka08.png)

>**HW(Hight Watermark)**:指的是消费者能见到的最大的offset；ISR中最小的LEO
>
>**LEO（Log End Offset）**：每个副本中最大的offset

1. follower故障

   follower故障时，`isr`会把当前副本剔除，待该follower恢复后，follower会读取本地磁盘记录的上次的HW，并将log文件高于HW的部分截掉，从HW开始向leader同步（基于leader多退少补），follower的LEO >= 该partition的HW后，就可以重新加入ISR了

2. leader故障

   leader发生故障后，isr会自动选取新的leader，为了保证副本数据的一致性，其余的follwer会先将各自的log文件高于HW的的部分截掉，然后从新的leader同步数据

  **HW只能保证副本间的一致性，并不能保证数据不丢失或者不重复**

### 2.2.5 Exactly Once（精准一次性）语义

​    ACK=-1，可以保证Producer到Server间不丢数据，即`At Least Once`

​			可以保证数据不丢失，但不能保证数据不重复

​    ACK=0, Producer只会发送一次数据，即`At Most Once`

   0.11版本以前，无法做到数据既不丢失，也不重复，也就是做到`Exactly Once`,
   0.11后，kafka引入了一个重大特性：幂等性。

> 幂等性: 生产者不论发送多少条消息，server都只会持久化一次。

  <strong style="color:red"> **`At Least Once` + 幂等性 =  `Exactly Once`**</strong>

   启用幂等性：

```
# Produce配置, 设为true，默认ack=-1
enable.idempotence=true
```

  开启幂等性的Producer在初始化时kafka分配给一个PID，发往同一个Patition的消息会附带Sequence Number，
Borker端会对<PID,Partition,SeqNumber>做缓存，当具有相同主键的消息提交时，Broker只会持久化一条。

  PID是Broker分配的， 当Porducer端重启会发生变化。

<strong style="color:red">幂等性无法保证跨区跨会话的`Exactly Once`</strong>

### 2.2.6 事务

## 2.3 数据丢失情况

- ACK=0，网络异常。客户端无法感知消息是否发送成功
- ACK=1，Leader副本异常。Leader副本接受成功，返回确定信息给客户端，Follower副本还未同步完成，Leader节点故障
- 客户端异常。Async异步发送模式下，客户端发送消息并没有直接发给kafka，而是在Client端按一定规则缓存并批量发送，在此期间，如果客户端死机，消息丢失
- 缓冲区满。异步模式下，Client端缓存大小超出缓冲池的大小，也可能发生消息丢失

# 三、消费者

## 3.1 消费方式

consumer采用**pull**（拉）模式从broker中读取数据。

push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由broker决定的。它的目标是尽可能以最快速度传递消息，但是这样很容易造成consumer来不及处理消息，典型的表现就是拒绝服务以及网络拥塞。而pull模式则可以根据consumer的消费能力以适当的速率消费消息。

pull模式不足之处是，如果kafka没有数据，消费者可能会陷入循环中，一直返回空数据。针对这一点，Kafka的消费者在消费数据时会传入一个时长参数timeout，如果当前没有数据可供消费，consumer会等待一段时间之后再返回，这段时长即为timeout。

## 3.2 分区消费策略

### 3.2.1 RoundRobin

轮询分配，把整个消费者组订阅的topic当成一个整体来看。

比如：T1（P1、P2、P3）、T2（P1、P2、P3）、同一个消费者组的C1、C2

kafka以TopicAndPartition类为单位排序后进行轮询分配

假设第一次T1P1分配给了C1，则T1P2就分配给C2，以此类推：

​	C1：T1P1、T1P3、T2P2
​    C2：T1P2、T2P1、T2P3

 RoundRobin使得每个消费者分配的分区数相差最大值为1，负载均衡。

### 3.2.2 Range（范围分区）

kafka默认分区规则

是以单个topic为单位分配的。

例如T1有7个分区，T2有7个分区，同一个消费者组的C1、C2、C3

7/3 = 2 
C1分配T1 3个分区，分配T2 3个分区
C2分配T1 2个分区，分配T2 2个分区
C3分配T1 2个分区，分配T2 2个分区

每个消费者分区数相差值可能会很大，每个消费者的负载不均衡

## 3.3 offset的存储

由于consumer在消费过程可能会出现断电宕机等故障，consumer恢复后，需要从故障前的位置继续消费，所以consumer需要实时记录自己消费到了哪个offset，以便故障恢复后继续消费。

offset存储在kafka或者zookeeper中，具体存在哪看连接的消费者的初始化方式。

offset的存储的key是：<groupId, topic, partition> --offset，这样消费组重新分配分区时不会重复消费消息

![image-20210202233010758](D:\myself\springboot-example\文档\typora\images\kafka09.png)

## 3.4 重复消费数据

**终极原因就是消费端消费了数据，但offset没有提交成功**。

- 消费端宕机，offset没有提交

- 设置offset为自动提交，关闭kafka时，如果在close之前，调用 consumer.unsubscribe() 则有可能部分offset没提交，下次重启会重复消费

- 重新分配分区，触发rebalance，可能出现从头开始消费，导致重发问题

  一次消费数据处理时间太长，kafka session timeout，partition断开连接，重新分区。

  poll间隔时间：`max.poll.interval.ms` (默认300s)

  消费者在处理完一批poll数据时，提交offset报的错：由于处理该轮数据耗时太长，超过了300s，与session一样，kafka认为消费端挂了，断开连接，提交offset异常

# 四、zookeeper在kafka中的作用

   <strong style="color:red">Zookeeper 主要用于在集群中不同节点之间进行通信</strong>

![image-20210202233311796](D:\myself\springboot-example\文档\typora\images\kafka10.png)

# 五、Rebalance

当 Consumer Group 完成 Rebalance 之后，每个 Consumer 实例都会定期地向 Coordinator 发送心跳请求，表明它还存活着。

```
session.timeout.ms: 10s # 10s后Coordiantor没有收到Consumer的心跳，将其移出Group
heartbeat.interval.ms：2s #每隔2sConsumer发送一个心跳请求
```

```
max.poll.interval.ms: 5min
限定了 Consumer 端应用程序两次调用 poll 方法的最大时间间隔
Consumer 程序如果在 5 分钟之内无法消费完 poll 方法返回的消息，那么 Consumer 会主动发起 “离开组” 的请求，Coordinator 也会开启新一轮 Rebalance
```

Rebalance: 即对kafka中分区进行重新分配的过程

## 5.1 触发场景

- 同一个消费组下新增消费者
- 消费者宕机、主动取消订阅（kafka与消费者断开连接）
- 新增分区

## 5.2 操作过程：

  Kafka提供了一个Coordinator来管理消费端group

①  确定Coordinator：

​	第一个启动的Consumer向kafka发送GroupCoordinateRequest请求，kafka在broker集群中选取一个负载最小的broke作为Coordinator，并把broker.id返回

②  JoinGroup

​	确定了Coordinator，所有的消费者都会向Coordinator发送一个JoinGroup请求（只要启动就会发送），Coordinator在Group中选取一个leader，并返回组员信息和订阅信息。

根据下图，可以看出，只有leader才有消费者的订阅信息。

leader根据分区分配策略决定分区情况

<img src="D:\myself\springboot-example\文档\typora\images\kafka11.png" alt="image-20210607221605415" style="zoom:150%;" />

①  Synchronizing Group State 阶段

该阶段主要完成将leader的分配方案同步给Group下的所有Consumer

完成分区分配后，所有Consumer都会发送SyncGroup请求，只有leader会发送分配方案，其他Consumer也会发送只不过是空值，Coordinator向所有Consumer返回分配方案。这样所有的成员都会知道自己消费的分区。

![image-20210607222012132](D:\myself\springboot-example\文档\typora\images\kafka12.png)



 <strong style="color:red">kafka的分区分配方案是Consumer实现的，这样更具有灵活性</strong>

# 六、Controller Broker

https://zhuanlan.zhihu.com/p/165989024

# 七、Leader选举

## 7.1 controller选举

controller的选举是通过broker在zookeeper的"/controller"节点下创建临时节点来实现的，并在该节点中写入当前broker的信息

先到先得

当controller宕机或者和zookeeper失去连接时，zookeeper检测不到心跳，zookeeper上的临时节点会被删除，而其它broker会监听临时节点的变化，当节点被删除时，其它broker会收到通知，重新发起controller选举

## 7.2  leader的选举【分区副本的leader】

分区leader的选举由 controller 负责管理和实施。

当leader发生故障时，controller会将leader的改变直接通过RPC的方式通知需要为此作出响应的broker，需要为此作出响应的broker即该分区的ISR集合中follower所在的broker，kafka在zookeeper中动态维护了一个ISR，只有ISR里的follower才有被选为Leader的可能。

具体过程是这样的：按照AR集合中副本的顺序 查找到 第一个 存活的、并且属于ISR集合的 副本作为新的leader。一个分区的AR集合在创建分区副本的时候就被指定，只要不发生重分配的情况，AR集合内部副本的顺序是保持不变的，而分区的ISR集合上面说过因为同步滞后等原因可能会改变，所以注意这里是根据AR的顺序而不是ISR的顺序找。

※ 对于上面描述的过程我们假设一种极端的情况，如果partition的所有副本都不可用时，怎么办？这种情况下kafka提供了两种可行的方案：

1、选择 ISR中 第一个活过来的副本作为Leader；

2、选择第一个活过来的副本（不一定是ISR中的）作为Leader；

这就需要在可用性和数据一致性当中做出选择，如果一定要等待ISR中的副本活过来，那不可用的时间可能会相对较长。选择第一个活过来的副本作为Leader，如果这个副本不在ISR中，那数据的一致性则难以保证。kafka支持用户通过配置选择，以根据业务场景在可用性和数据一致性之间做出权衡。

## 7.3 消费组leader的选举

​	组协调器会为消费组（consumer group）内的所有消费者选举出一个leader，这个选举的算法也很简单，第一个加入consumer group的consumer即为leader，如果某一时刻leader消费者退出了消费组，那么会重新 随机 选举一个新的leader

# 八、保证高可用

https://juejin.cn/post/6948023337436053541

## 8.1 备份机制

Kafka允许同一个Partition存在多个消息副本，每个Partition的副本通常由1个Leader及0个以上的Follower组成，生产者将消息直接发往对应Partition的Leader，Follower会周期地向Leader发送同步请求

同一Partition的Replica不应存储在同一个Broker上，因为一旦该Broker宕机，对应Partition的所有Replica都无法工作，这就达不到高可用的效果

所以Kafka会尽量将所有的Partition以及各Partition的副本均匀地分配到整个集群的各个Broker上

## 8.2 ISR机制

## 8.3 ACK机制

## 8.4 故障恢复机制

## 8.5 Controller

















