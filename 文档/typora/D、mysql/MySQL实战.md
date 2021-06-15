# 一、基础架构

<img src="D:\myself\springboot-example\文档\typora\images\mysql18.png" alt="img" style="zoom:50%;" />

## 1.1 缓存器

再建立连接后，需要查询缓存，如果缓存中有，直接返回，否则继续。

缓存器存储的是key-value，key是查询语句，value是查询结果

不建议使用缓存，因为缓存在修改表时会被清空,MySQL8.0移除了缓存器

```shell
# 禁用缓存
query_cache_type:DEMAND
```

# 二、日志系统

## 2.1 redo log（重做日志）

### 2.1.1 概念

**记录的是数据页的改动**

>WAL(Write-Ahead Logging) 先写日志，再写磁盘

每次都写入磁盘缺点：

​	每次更新操作如果都写入磁盘，需要先查找记录，然后再写入，整个过程IO成本、查找成本都很高。

redo-log过程：

​	更新记录时，先写redo log，再更新内存。等到合适时机，再把redo log操作记录写入磁盘

结构：

​	固定大小，可以配置成一组4个文件，每个文件1G。

​    `write pos` 当前记录的位置，写入后移

​	`checkpoint` 当前擦除的位置，擦除后移。擦除前需要写入磁盘

​	当 `write pos` 追上 `checkpoint`时，代表redo log文件快写满了，需要将他们两个之间的数据写入磁盘

**crash-safe**

​	保证数据库发生异常重启，之间的数据也不会丢失

### 2.1.2 flush磁盘

> 当内存数据页跟磁盘数据页内容不一致的时候，我们称这个内存页为“脏页”。内存数据写入到磁盘后，内存和磁盘上的数据页的内容就一致了，称为“干净页”

redo log flush时机：

- redo log写满
- 系统内存不足。
- 系统空闲时
- mysql正常关闭

InnoDB 的刷盘速度就是要参考这两个因素：一个是脏页比例，一个是 redo log 写盘速度

## 2.2 binlog

二进制文件

### 2.2.1 作用

记录了mysql的数据更新；在mysql主从复制中依赖了binlog

binlog可以用于归档，binlog采用追加写的方式，所有的改动都在binlog中；而redolog是循环写，写到一定程序就需要更新磁盘中的记录，无法归档。

### 2.2.2 类型

- **Row Level**

  日志中会记录每一行数据被修改前后的情况，然后在slave端对相同的数据进行修改，但数据量太大

- **Statement Level**（默认）

  - 简介：每一条被修改数据的sql都会记录到master的bin-log中，slave在复制的时候sql进程会解析成和原来master端执行过的相同的sql再次执行。在主从同步中一般是不建议用statement模式的，因为会有些语句不支持，比如语句中包含UUID函数，以及LOAD DATA IN FILE语句等
  - 优点：解决了 Row level下的缺点，不需要记录每一行的数据变化，减少bin-log日志量，节约磁盘IO，提高新能
  - 缺点：容易出现主从复制不一致

- **Mixed**（混合模式）

  结合了Row level和Statement level的优点，同时binlog结构也更复杂

## 2.3 redo log和binlog的区别

- redo log是InnoDB引擎特有的，binlog是Server层实现的，所有引擎都可以使用
- redo log是物理日志，binlog是逻辑日志
- redo log是循环写的；binlog是追加写的。“追加写”是指binlog文件写到一定大写会切换到另一个文件中，并不会覆盖
- binlog可以作为恢复数据使用，主从复制搭建，redo log作为异常宕机或者介质故障后的数据恢复使用

## 2.4 两阶段提交

`update table set c=c+1 where id=2` 更新流程

<img src="D:\myself\springboot-example\文档\typora\images\mysql19.png" alt="img" style="zoom: 50%;" />	

**两阶段提交的目的：为了两份日志之间的逻辑一致**

# 三、事务隔离

> 事务：是数据库操作的最小工作单元，是作为单个逻辑工作单元执行的一系列操作；这些操作作为一个整体一起向系统提交，要么都执行、要么都不执行；事务是一组不可再分割的操作集合（工作逻辑单元）

实现上，数据库会创建一个视图，访问时以视图的逻辑结果为准

- 读未提交（read uncommitted）：返回记录上的最新值
- 读提交（read committed）：是在真正执行sql时创建的试图
- 可重复读（repeatable read）：一启动事务就创建视图
- 串行化（serializable ）：加锁防止并行访问

```shell
show variables like 'transaction_isolation'
```

## 3.1 事务隔离的实现

在 MySQL 中，实际上每条记录在更新的时候都会同时记录一条回滚操作。记录上的最新值，通过回滚操作，都可以得到前一个状态的值

以可重复读为例：

​	是不是在read-viewA中将1更新成了2？

​	对于 read-view A，要得到 1，就必须将当前值依次执行图中所有的回滚操作得到

**当系统里没有比这个回滚日志（undo log）更早的 read-view 的时候，该回滚日志会被删除**

<img src="D:\myself\springboot-example\文档\typora\images\mysql20.png" alt="img" style="zoom:50%;" />



**尽量不要使用长事务**：

​	长事务意味着系统里面会存在很老的事务视图。由于这些事务随时可能访问数据库里面的任何数据，所以这个事务提交之前，数据库里面它可能用到的回滚记录都必须保留，这就会导致大量占用存储空间

## 3.2 启动方式

- 显示启动。begin、start transaction
- set autocommit=0
- commit work and chain 提交事务并自动开启下一个事务

```sql
# 查找长事务
select * from information_schema.innodb_trx where TIME_TO_SEC(timediff(now(),trx_started))>60
```

## 3.3 “快照”在 MVCC 里是怎么工作的？

<strong style="color:red">InnoDB 利用了“所有数据都有多个版本”的这个特性，实现了“秒级创建快照”的能力</strong>

### 3.3.1 view分类

- 普通view。它是一个用查询语句定义的虚拟表，在调用的时候执行查询语句并生成结果。
-  InnoDB 在实现 MVCC 时用到的一致性读视图，即 consistent read view，用于支持 RC（Read Committed，读提交）和 RR（Repeatable Read，可重复读）隔离级别的实现。

### 3.3.2 当前事务的一致性视图（read-view）

​	 `row trx_id` 每次事务更新数据的时候，都会生成一个新的数据版本，并且把事务ID 赋值给这个数据版本

​	 数据表中的一行记录，其实可能有多个版本 (row)，每个版本有自己的 row trx_id。

​										                                         **行状态变更图** 虚线为undo log回滚日志

<img src="D:\myself\springboot-example\文档\typora\images\mysql22.png" alt="img" style="zoom:50%;" />

​	 V1、V2、V3 并不是物理上真实存在的，而是每次需要的时候根据当前版本和 undo log 计算出来的。比如，需要 V2 的时候，就是通过 V4 依次执行 U3、U2 算出来

​	InnoDB 为每个事务构造了一个数组，用来保存这个事务启动瞬间，当前正在“活跃”的所有事务 ID。“活跃”指的就是，启动了但还没提交

​	**低水位**： 数组里面事务 ID 的最小值

​	**高水位**：当前系统里面已经创建过的事务 ID 的最大值加 1

**这个视图数组和高水位，就组成了当前事务的一致性视图（read-view）**

### 3.3.3 数据版本可见性规则

<img src="D:\myself\springboot-example\文档\typora\images\mysql23.png" alt="img" style="zoom: 50%;" />

对于当前事务的启动瞬间来说，一个数据版本的 row trx_id，有以下几种可能：

- 如果落在绿色部分，表示这个版本是已提交的事务或者是当前事务自己生成的，这个数据是可见的；
- 如果落在红色部分，表示这个版本是由将来启动的事务生成的，是肯定不可见的；
- 如果落在黄色部分
  - a. 若 row trx_id 在数组中，表示这个版本是由还没提交的事务生成的，不可见；
  - b. 若 row trx_id 不在数组中，表示这个版本是已经提交了的事务生成的，可见。

 具体：如果有一个事务，它的低水位是 18（落在黄色部分），那么当它访问这一行数据时，就会从 V4 通过 U3 计算出 V3，所以在它看来，这一行的值是 11

### 3.3.4 当前读

​	除了 update 语句外，select 语句如果加锁，也是当前读。

### 3.3.5 举例

```shell
# 并不是一个事务的起点，在执行到它们之后的第一个操作 InnoDB 表的语句，事务才真正启动
begin/start transaction
# 马上启动一个事务
start transaction with consistent snapshot
```

结果：事务A k=1 事务B：k=3

<img src="D:\myself\springboot-example\文档\typora\images\mysql21.png" alt="img" style="zoom: 67%;" />

事务 A 的视图数组就是[99,100], 事务 B 的视图数组是[99,100,101], 事务 C 的视图数组是[99,100,101,102]

<img src="D:\myself\springboot-example\文档\typora\images\mysql24.png" alt="img" style="zoom:50%;" />



> 高水位：
>
> ​	在实现上， InnoDB 为每个事务构造了一个数组，用来保存这个事务启动瞬间，当前正在“活跃”的所有事务 ID。“活跃”指的就是，启动了但还没提交。数组里面事务 ID 的最小值记为低水位，当前系统里面已经创建过的事务 ID 的最大值加 1 记为高水位。 所以当事务A创建时，高水位=99+1=100

#### 3.3.5.1 事务A读取数据

- 找到 (1,3) 的时候，判断出 row trx_id=101，比高水位大，处于红色区域，不可见；
- 接着，找到上一个历史版本，一看 row trx_id=102，比高水位大，处于红色区域，不可见；
- 再往前找，终于找到了（1,1)，它的 row trx_id=90，比低水位小，处于绿色区域，可见

总结：

- 版本未提交，不可见；
- 版本已提交，但是是在视图创建后提交的，不可见；
- 版本已提交，而且是在视图创建前提交的，可见

**事务A读取数据：**

- (1,3) 还没提交，属于情况 1，不可见；
- (1,2) 虽然提交了，但是是在视图数组创建之后提交的，属于情况 2，不可见；
- (1,1) 是在视图数组创建之前提交的，可见。

#### 3.3.5.2 更新数据

​	事务 B 的 update 语句，如果按照一致性读，好像结果不对哦？

​	事务 B 的视图数组是先生成的，之后事务 C 才提交，不是应该看不见 (1,2) 吗，怎么能算出 (1,3) 来？

<strong style="color:red">更新数据都是先读后写的，而这个读，只能读当前的值，称为“当前读”（current read）</strong>

​	在更新的时候，当前读拿到的数据是 (1,2)，更新后生成了新版本的数据 (1,3)，这个新版本的 row trx_id 是 101

## 3.4 事务的可重复读的能力是怎么实现的

​	可重复读的核心就是一致性读（consistent read）；

​	而事务更新数据的时候，只能用当前读。如果当前的记录的行锁被其他事务占用的话，就需要进入锁等待

## 3.5 幻读

[20|幻读是什么，幻读有什么问题？](https://time.geekbang.org/column/article/75173)

​	可重复读模式下，只加行锁/表锁，导致幻读，并且binlog和数据库的数据不一致

​	`next-key lock` 间隙锁和行锁的合称，前开后闭

- **间隙锁的引入，可能会导致同样的语句锁住更大的范围，影响了并发度**， 容易造成死锁：
- 间隙锁是在可重复读隔离级别下才会生效的
- 读提交隔离级别下没有间隙锁。需要解决可能出现的数据和日志不一致问题，需要把 binlog 格式设置为 row

![img](D:\myself\springboot-example\文档\typora\images\mysql28.png)

# 四、索引

<strong style="color:orange">索引的出现其实就是为了提高数据查询的效率</strong>

## 4.1 常见模型

- 哈希表：适用于等值查询的场景；但区间查询的速度很慢
- 有序数组：等值查询和范围查询场景中的性能都非常优秀；更新数据麻烦
- 搜索树

​     树可以有二叉，也可以有多叉。多叉树就是每个节点有多个儿子，儿子之间的大小保证从左到右递增。二叉树是搜索效率最高的，但是实际上大多数的数据库存储却并不使用二叉树。其原因是，**索引不止存在内存中，还要写到磁盘上**

​	以 InnoDB 的一个整数字段索引为例，这个 N 差不多是 1200。这棵树高是 4 的时候，就可以存 1200 的 3 次方个值，这已经 17 亿了。考虑到树根的数据块总是在内存中的，一个 10 亿行的表上一个整数字段的索引，查找一个值最多只需要访问 3 次磁盘。其实，树的第二层也有很大概率在内存中，那么访问磁盘的平均次数就更少了

## 4.2 InnoDB的索引模型

​	表根据主键顺序以索引顺序存放。

​	InnoDB使用B+树

### 4.2.1 主键索引和非主键索引

​	主键索引：也称为聚簇索引。存放的是整行数据

​	非主键索引：存放的是主键id。先查询到主键id，然后去主键索引上根据id查找数据：**回表**

### 4.2.2 页分裂和页合并

​	页分裂：数据页已满，需要插入一条记录，需要将该页的部分数据挪到下页

​	页合并：当相邻两个页由于删除了数据，利用率很低之后，会将数据页做合并

### 4.2.3 联合索引

​	联合索引中可以使用覆盖索引来提高查找效率

### 4.2.4 最左前缀原则

​	索引项是按照索引定义里面出现的字段顺序排序的

​	最左前缀可以是联合索引的最左 N 个字段，也可以是字符串索引的最左 M 个字符

```sql
-- 创建(name,age)的联合索引
select * from tuser where name like '张%' and age=10 and ismale=1;
```

在 MySQL 5.6 之前，只能从 ID3 开始一个个回表。到主键索引上找出数据行，再对比字段值。

而 MySQL 5.6 引入的**索引下推**优化（index condition pushdown)， 可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录，减少回表次数

## 4.3 重建索引	

​	索引可能因为删除，或者页分裂等原因，导致数据页有空洞，重建索引的过程会创建一个新的索引，把数据按顺序插入，这样页面的利用率最高，也就是索引更紧凑、更省空间

```sql
-- 合理
alter table T drop index k;
alter table T add index(k);

-- 不合理 不论是删除主键还是创建主键，都会将整个表重建
-- 代替：alter table T engine=InnoDB
alter table T drop primary key;
alter table T add primary key(id);
```

## 4.4 change buffer

​	在内存中有拷贝，也会写入磁盘

```shell
# change buffer 的大小最多只能占用 buffer pool 的 50%
innodb_change_buffer_max_size=50
```

### 4.4.1执行过程

​	当需要更新一个数据页时，如果数据页在内存中就直接更新。

​	否则将更新操作缓存在 `change buffer` 中，在下次访问该数据页时，将数据页读取内存，然后merge

### 4.4.2 merge

> 将 change buffer 中的操作应用到原数据页，得到最新结果的过程

执行流程：

	1. 从磁盘读入数据页到内存（老版本的数据页）
 	2. 从 change buffer 里找出这个数据页的 change buffer 记录 (可能有多个），依次应用，得到新版数据页；
 	3. 写 redo log。这个 redo log 包含了数据的变更和 change buffer 的变更

​    **到这里 merge 过程就结束了。这时候，数据页和内存中 change buffer 对应的磁盘位置都还没有修改，属于脏页，之后各自刷回自己的物理数据，就是另外一个过程了**

触发merge的场景：

- 访问该数据页
- 后台线程定期merge
- 数据库正常关闭（shutdown）

### 4.4.3 作用

​	将数据从磁盘读入内存涉及随机 IO 的访问，是数据库里面成本最高的操作之一。

​	change buffer 减少了随机磁盘访问，对更新性能的提升是很明显的

### 4.4.4 适用场景

​	<strong style="color:blue">唯一索引的更新不能使用 change buffer，只有普通索引可以使用</strong>

​	适用于写多读少的场景

### 4.4.5 redo log 和change buffer

**优化了整个变更流程的不同阶段。** 

(insert、update、delete)流程： 

​	1、从磁盘读取待变更的行所在的数据页，读取至内存页中。

​		 **涉及 随机 读磁盘IO** 

​		 `Change buffer` 避免了随机读磁盘IO

​	2、对内存页中的行，执行变更操作 

​	3、将变更后的数据页，写入至磁盘中。 **涉及 随机 写磁盘IO** 

​		 `redo log` 避免了随机写磁盘IO

 	有无用到change buffer机制，对于redo log这步的区别在于—— 用到了change buffer机制时，在redo log中记录的本次变更，是记录new change buffer item相关的信息，而不是直接的记录物理页的变更。

## 4.5 普通索引和唯一索引

### 4.5.1 查询过程

```
select id from T where k=5
```

InnoDB 的数据是按数据页为单位来读写的。两者之间的性能差异微乎其微

### 4.5.2 更新过程

[changer buffer](##4.4 change buffer)

​	唯一索引的更新就不能使用 change buffer，实际上也只有普通索引可以使用

## 4.6 优化器选择索引

不断地删除历史数据和新增数据，MySQL 会选错索引

优化器选择索引：

- 扫描行数
- 是否使用临时表
- 是否排序

### 4.6.1 判断扫描行数

## 4.7 字符串字段索引

使用前缀索引，定义好长度，就可以做到既节省空间，又不用额外增加太多的查询成本

```shell
# 无法使用覆盖索引
alter table SUser add index index2(email(6));
```



# 五、锁机制

## 5.1 全局锁

​	对整个数据库实例加锁，整个库处于只读状态

​	全局锁的典型使用场景是，做全库逻辑备份

```
Flush tables with read lock (FTWRL)
```

## 5.2 表级锁

表锁，一种是元数据锁（meta data lock，MDL)

### 5.2.1 表锁

```
lock tables … read/write
unlock tables/客户端断开的时候自动释放
```

### 5.2.2 MDL（metadata lock)

​	MDL 不需要显式使用，在访问一个表的时候会被自动加上

​	当对一个表做增删改查操作的时候，加 MDL 读锁；当要对表做结构变更操作的时候，加 MDL 写锁

- 读锁之间不互斥，因此你可以有多个线程同时对一张表增删改查。
- 读写锁之间、写锁之间是互斥的，用来保证变更表结构操作的安全性。

## 5.3 行锁

​	**两阶段锁协议** 在 InnoDB 事务中，行锁是在需要的时候才加上的，但并不是不需要了就立刻释放，而是要等到事务结束时才释放

### 5.3.1 死锁和死锁检测

![img](https://static001.geekbang.org/resource/image/4d/52/4d0eeec7b136371b79248a0aed005a52.jpg)

解决死锁：

- 一种策略是，直接进入等待，直到超时。这个超时时间可以通过参数 innodb_lock_wait_timeout （默认50s）来设置。
- 另一种策略是，发起死锁检测，发现死锁后，主动回滚死锁链条中的某一个事务，让其他事务得以继续执行。将参数 innodb_deadlock_detect 设置为 on，表示开启这个逻辑。

## 5.4 next-key lock

加锁规则：两个原则、两个优化、和一个bug

- 原则 1：加锁的基本单位是 next-key lock。前开后闭区间。
- 原则 2：查找过程中访问到的对象才会加锁。
- 优化 1：索引上的等值查询，给唯一索引加锁的时候，next-key lock 退化为行锁。
- 优化 2：索引上的等值查询，向右遍历时且最后一个值不满足等值条件的时候，
- next-key lock 退化为间隙锁。
- 一个 bug：唯一索引上的范围查询会访问到不满足条件的第一个值为止。



```mysql
CREATE TABLE `t` 
	( `id` int(11) NOT NULL, 
		`c` int(11) DEFAULT NULL, 
		`d` int(11) DEFAULT NULL, 
		PRIMARY KEY (`id`), 
		KEY `c` (`c`)
	) ENGINE=InnoDB;
insert into t values(0,0,0),(5,5,5),(10,10,10),(15,15,15),(20,20,20),(25,25,25);
```



### 5.4.1 等值查询间隙锁

1. 根据原则1，sessionA next-key lock区间(5,10]
2. 优化1，id=7不存在，优化2，

![img](D:\myself\springboot-example\文档\typora\images\mysql29.png)

# 六、空间回收

```shell
# OFF:表的数据放在系统共享表空间，也就是跟数据字典放在一起；
# ON: 每个 InnoDB 表数据存储在一个以 .ibd 为后缀的文件中。
innodb_file_per_table=ON
```

delete 命令其实只是把记录的位置，或者数据页标记为了“可复用”，但磁盘文件的大小是不会变的

通过 delete 命令是不能回收表空间的。这些可以复用，而没有被使用的空间，看起来就像是“空洞

如果数据是按照索引递增顺序插入的，那么索引是紧凑的。但如果数据是随机插入的，就可能造成索引的数据页分裂

## 6.1 重建表

经过大量增删改的表，都是可能是存在空洞的。所以，如果能够把这些空洞去掉，就能达到收缩表空间的目的

# 七、命令解析

## 7.1 count

### 7.1.1 count(*)的实现方式

- MyISAM 引擎把一个表的总行数存在了磁盘上，因此执行 count(*) 的时候会直接返回这个数，效率很高；*
- *而 InnoDB 引擎就麻烦了，它执行 count(*) 的时候，需要把数据一行一行地从引擎里面读出来，然后累积计数 

### 7.1.2 count用法

- `count(主键 id) ` InnoDB 引擎会遍历整张表，把每一行的 id 值都取出来，返回给 server 层。server 层拿到 id 后，判断是不可能为空的，就按行累加。
- ` count(1) `，InnoDB 引擎遍历整张表，但不取值。server 层对于返回的每一行，放一个数字“1”进去，判断是不可能为空的，按行累加。单看这两个用法的差别的话，你能对比出来，count(1) 执行得要比 count(主键 id) 快。因为从引擎返回 id 会涉及到解析数据行，以及拷贝字段值的操作。
- `count(字段)`  如果这个“字段”是定义为 not null 的话，一行行地从记录里面读出这个字段，判断不能为 null，按行累加；
- `count(*)` 专门做了优化

**count(字段)<count(主键 id)<count(1)≈count(*)**

## 7.2 order by

### 7.2.1 全字段排序

`sort_buffer_size` MySQL 为排序开辟的内存（sort_buffer）的大小。

- 如果要排序的数据量小于 sort_buffer_size，排序就在内存中完成。**快速排序**
- 但如果排序数据量太大，内存放不下，则不得不利用磁盘临时文件辅助排序  **归并排序**

### 7.2.1 rowid排序

如果查询要返回的字段很多的话，那么 sort_buffer 里面要放的字段数太多，这样内存里能够同时放下的行数很少，要分成很多个临时文件，排序的性能会很差

```
# 如果单行长度超过16，就采用rowid排序
SET max_length_for_sort_data = 16;
```

select city,name,age from t where city='杭州' order by name limit 1000  ;

执行流程：

	1. 初始化 `sort buffer`
 	2. 查找city索引找到第一个等于杭州的主键id，
 	3. 去主键索引中找到整行，取出name和id存入`sort buffer`
 	4. 从索引city取出下一个记录的主键id，继续3操作，直到找完
 	5. 对`sort buffer`中的数据根据name排序，去前1000条数据，然后根据id的值再去聚簇索引中找到查找的字段

## 7.3 索引失效

### 7.3.1 索引字段做函数操作

```mysql
# 破环了t_modified索引的有序性，改为全索引扫描
select count(*) from tradelog where month(t_modified)=7;

# 修改
select count(*) from tradelog where
     (t_modified >= '2016-7-1' and t_modified<'2016-8-1') or
     (t_modified >= '2017-7-1' and t_modified<'2017-8-1') or 
     (t_modified >= '2018-7-1' and t_modified<'2018-8-1');
```

<strong style="color:red">对索引字段做函数操作，可能会破坏索引值的有序性，因此优化器就决定放弃走树搜索功能</strong>

### 7.3.2 隐式类型转换

> Mysql字符串和数字作比较：字符串将转为数字

```mysql
# tradeid 为varchar类型，将转换
select * from tradelog where tradeid=110717;
<=>
select * from tradelog where  CAST(tradid AS signed int) = 110717;
```

### 7.3.3 隐式字符编码转换

两个不同编码的字段做比较，如果索引字段将转换编码，就触发了7.3.1中的对索引字段做函数操作，也会使索引失效

## 7.4 查询一行数据执行慢

先排除Mysql数据本身压力就很大，导致CPU占用率很高或IO利用率很高

```
select * from t where id=1;
```

### 7.4.1 查询长时间不返回

 ```msyql
# 查看当前语句处于什么状态
show processlist
 ```

- **等MDL锁**

  ![img](D:\myself\springboot-example\文档\typora\images\mysql25.png)

  解决：

     杀死造成阻塞的进程

  ![img](D:\myself\springboot-example\文档\typora\images\mysql26.png)

- **等flush**

- **等行锁**

### 7.4.2 查询慢

​	**一致性读**

​		对于sessionA 第一次查询是一致性读，需要从10001开始，一次一次undo log，执行100W次后，才将1结果返回

​		而对于lock in share mode下是当前读，直接返回10001

![img](D:\myself\springboot-example\文档\typora\images\mysql27.png)

# MVCC

# 配置

```shell
# 禁用缓存
query_cache_type=DEMAND

# 每次事务的redolog直接持久化到磁盘
innodb_flush_log_at_trx_commit=1
# 每次事务的binlog都持久化到磁盘
sync_binlog=1
```

















