# 一、概述

>Kafka是一个**发布式**的基于**发布/订阅**模式的消息队列，主要应用于大数据实时处理领域

Kafka 是linkedin 公司用于日志处理的分布式消息队列，同时支持离线和在线日志处理。kafka 对消息保存时根据Topic进行归类，发送消息者成为Producer,消息接受者成为Consumer,此外kafka 集群有多个kafka 实例组成，每个实例(server)称为broker。无论是kafka集群，还是producer和consumer 都依赖于zookeeper 来保证系统可用性，为集群保存一些meta 信息。

## 1.1 消息队列的两种模式

**1. 点对点模式**

​	一对一，消费者主动拉取数据，消息收到后消息清除
​    消息被消费以后，queue中不再有存储，所以消息消费者不可能消费到已经被消费的消息。Queue支持存在多个消费者，但是对一个消息而言，只会有一个消费者可以消费

**2. 发布订阅模式**

  一对多，并且有两种消费模式，一是队列主动推送，二是消费者基于长连接轮询主动拉取数据，kafka属于后者

## 1.2 优点：

><strong style="color:red">生产者写入数据采用：顺序写入和页缓存技术 </strong>
>
><strong style="color:red">消费者读取数据采用：零拷贝技术 </strong>

- 首先说一下，为什么顺序写入比随机写入快：先介绍一下它的存储原理。机械硬盘的结构你可以想象成一个唱片机，它有一个旋转的盘片和一个能沿半径方向移动的磁头。处理读取和写入请求时，首先可以根据请求的开始地址算出要处理的数据在磁盘上的位置，之后要进行以下几步工作：1、磁头沿半径方向移动，直至移动到数据所在的柱面（相同半径的磁道组成的环面）2、盘片高速旋转，使磁头到达数据的起始位置3、磁头沿磁道从磁盘读取或写入数据。当一次读取的数据量很少的时候，1、2步骤带来的开销是无法忽略的，这使得随机写相对于顺序写会有巨大的性能劣势。因为在顺序写的时候，1、2步骤只需要执行一次，剩下的全是数据传输所需要的固有开销；而每次随机写的时候，前两个步骤都需要执行，带来了极大的额外开销。
- 其次再说一下**页缓存**：即便是顺序写入硬盘，硬盘的访问速度还是不可能追上内存。所以Kafka的数据并 不是实时的写入硬盘 ，它充分利用了现代操作系统 分页存储 来利用内存提高I/O效率。Memory Mapped Files也被翻译成 内存映射文件 ，在64位操作系统中一般可以表示20G的数据文件，它的工作原理是直接利用操作系统的Page来实现文件到物理内存的直接映射。完成映射之后你对物理内存的操作会被同步到硬盘上(操作系统在适当的时候)，也就是说，将**复杂的IO操作交给了操作系统**。

​	高吞吐、低延时

​	持久化、可靠性、容错性

​	高并发、可扩展

解耦，消峰，解决生产者速度>消费者速度的问题

## 1.2 kafka 文件存储机制

![image-20210201211711400](D:\myself\springboot-example\文档\typora\images\kafka04.png)

kafka中消息是以topic进行分类的，生产者生产消息，消费组消费消息，都是面向topic的

topic是逻辑概念，而partition是物理概念，每个partition对应一个log文件，该log文件存储生产者生产的数据，不断追加到该文件的尾端，且每条数据都有自己的offset。消费者组中的每个消费者，都会实时记录自身的offset，以便出错恢复时，从上次的位置继续消费。

![image-20210201212211947](D:\myself\springboot-example\文档\typora\images\kafka05.png)

每个分区目录下都有.index和.log文件，每个log文件存储的大小可配置，默认1G。分区采用分段机制存储，当超过1G时，再次创建一个log文件，文件名为自身最小的offset，当消费组消费消息时，首先根据自身的offset找到index对应的log文件偏移量，根据文件偏移量采用**二分查找**找到对应的log文件。

## 1.3 分区

   消费者默认50个分区consumer-offset-n

### 1.3.1 分区原因：

1. 方便在集群中扩展。每个分区可以通过调整以适应所在机器的存储大小，处理速度，而一个topic是由多个partition组成，这样整个集群就可以适应任意大小的数据了

 	2. 提高并发度。

### 1.3.2 分区策略（生产者）

​		生产者将消息封装成一个`ProducerRecord`对象

![image-20210202110638824](D:\myself\springboot-example\文档\typora\images\kafka06.png)

		1. 指定partition
  		2. 没有指明partition但有key的情况，将key的hash值与分区数取模后得到partition值
  		3. partition和key都没有指定，第一次调用时随机生成一个值然后取模得到partition，之后将这个值自增。这就是`round-robin`(轮询)算法

## 1.4 数据可靠性

**ack机制**确保数据成功发送到指定的topic，否则重发

![image-20210202111403037](D:\myself\springboot-example\文档\typora\images\kafka07.png)



### 1.4.1 副本同步策略

| **方案**                    | **优点**                                           | **缺点**                                             |
| --------------------------- | -------------------------------------------------- | ---------------------------------------------------- |
| 半数以上完成同步，就发送ack | 延 迟 低                                           | 选举新的leader时，容忍n台节点的故障，需要 2n+1个副本 |
| 全部完成同步，才发送ack     | 选举新的leader时，容忍n台节点的故障，需要n+1个副本 | 延迟高                                               |

kafka选择第二种方案，原因：

	1. 同样为了容忍n台节点的故障，第一种需要2n+1个节点确保半数以上原则，kafka每个分区都有大量数据，会造成大量数据的冗余
 	2. 虽然第二种方案的网络延迟会比较高，但网络延迟对Kafka的影响较小。

但第二种方案也有不足，如果有一个节点故障，那同步永远完成不了，kafka也无法发送ack，为了应对这个问题，kafka采取`ISR`机制

### 1.4.2 ISR

Leader维护了一个动态的`in-sync replica set `(ISR)，意为和leader保持同步的follower集合。

当ISR中的follower完成数据的同步之后，leader就会给follower发送ack。如果follower长时间未向leader同步数据，则该follower将被踢出ISR，该时间阈值由**replica.lag.time.max.ms**参数设定。Leader发生故障之后，就会从ISR中选举新的leader。

ISR存在leader和zookeepr中，是一个动态的队列，当超时（**replica.lag.time.max.ms**）或者主从分片同步数量超出阈值（**replica.lag.max.message**），ISR移除follower，之后又满足条件又加入了isr中。

在0.9版本后，移除了**replica.lag.max.message**配置，原因：当producer批量生产数量>**replica.lag.max.message**时，一定会导致ISR移除所有的follower，直到满足又加入了ISR，频繁的对ISR队列操作，会影响kafka性能。

### 1.4.3 ACK机制

对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，没必要等ISR中的follower全部接收成功。

参数配置

 1. acks=0

    producer不等待broker的ack，当broker故障时有可能**丢失数据**

 2. acks=1

    producer等待broker的ack，只要leader落盘成功就返回ack。如果follower同步之前leader故障，那么将会**丢失数据**

 3. acks=-1（all）

    producer等待broker的ack，必须等待leader和follower全部落盘成功才返回ack。如果在同步follower后，leader发送ack前，leader故障，会造成**数据重复**

    **数据重复**： 此时follower节点网络故障，没有在`isr`中，此时`isr`只有一个leader，当leader落盘并返回`ack`后，发生故障。这是极限情况，一般不会发生



















