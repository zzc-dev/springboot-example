# 一、优化SQL

## 1.1 SQL语句优化

- 尽量不使用select *
- 用join代替子查询
  - 子查询需要建临时表，查询完毕需要删除临时表
  - join中可以使用索引优化查询（Index Nested-Loop Join、Batched Key Access）
- 看情况使用exists和in，使用not exists代替not in

## 1.2 索引优化

对查询字段建立索引，并且主要查询不要让索引失效。

- like查询时，%尽量不放在最前面，字符串索引注意最左前缀原则
- 联合索引注意最左前缀原则
- where 应避免使用 != 导致索引失效
- where 应避免使用表达式、函数操作导致索引失效
- where中使用or，并且条件不全是索引列，导致索引失效

使用索引字段

- 更新太频繁的字段不适合做索引
- 在经常需要搜索的列上加索引
- 在经常需要根据范围搜索或者排序的列上添加索引，索引本是就是有序的

## 1.3 表结构或设计优化

- 能用数字或枚举就不用其他类型
- 字段尽可能设置为not null
- 固定长度的表会更快
- 长度越小的列越快

## 1.4  硬件和网络优化

## exists和in

- 外层查询表小于内层，使用exists，否则使用in

```csharp
// 遍历t1，看t2中是否存在t1的id，如果存在则加入结果集
select * from t1 a where exists (select * from t2 b where b.id = a.id)

// 遍历t2，将子查询的结果集存储起来，遍历t1表，满足条件加入结果集，执行次数t1.len*t2.len
select * from t1 a where a.id in (select b.id from t2 b)
```

- not in不会使用索引，因此使用not exists

## 字段为什么尽可能设置为not null

https://blog.csdn.net/LyySwx/article/details/97026389

null的缺点：

- **Null 列需要更多的存储空间：需要一个额外字节作为判断是否为 NULL 的标志位**
- 索引相关：b+树不存null，联合索引不存全为null的值，如果使用索引查询，可能会得到不符合预期的结果
- count(字段)统计不到null，count(*)可以
- not in子查询在有null值的情况下返回值永远为空

什么时候设为not null

  当使用唯一索引，又允许值为空时，唯一索引可以有多个null

  null值代表位置，null值无法比较

# 二、 select顺序

```
语法顺序：
select distinct from join on where group by having union order by limit

执行顺序
from
where
join
on
group by
having
select
distinct
union
order by
limit
```

# 三、命令比较

## 3.1 join

- left join
- right join
- inner join 查找的数据是两张表共有的

## 3.2 delete和truncate

truncate       >    delete

不支持事务

不支持where条件，直接清空表

## 3.3 char varchar区别

char 存储定长数据，索引效率高，数据不足长度自动补空格

varchar 存储变长数据，存储效率没有char高，varchar类型的实际长度是它的值的长度+1，这个字节用于保存实际用了

# 四、索引

## 4.1 索引原理

索引的作用是数据的快速检索。快速检索的本质是数据结构

可以从hash、二叉树、B树、B+树的优缺点谈起

## 4.2 InnoDB和Myisam的区别

都使用了B+树，

InnoDB是聚簇索引，叶子节点存储着数据页，Myisam是非聚簇索引，叶子节点存着数据的物理地址

|          | InnoDB                       | Myisam                                 |
| -------- | ---------------------------- | -------------------------------------- |
| 索引     | 聚簇索引（叶子节点存储数据） | 非聚簇索引（叶子节点存储数据物理地址） |
| 事务     | 支持                         | 不支持                                 |
| 锁       |                              | 不支持行锁，在更新数据时需要锁表       |
| 外键     |                              | 不支持                                 |
| 全文索引 | 不支持                       |                                        |
| count    | 不保存                       | 用了一个变量保存总数                   |

## 4.3 索引的优缺点

优点：

- 加快数据查询速度，主要原因
- 唯一索引确保值的唯一性
- 可以加快表与表的连接
- 可以显著减少分组和排序时间

缺点：

- 创建和维护索引需要时间，数据量少时反而会浪费时间
- 索引需要占用额外的物理空间
- 索引只适合查询，会降低表的增删改效率，需要对索引进行动态维护

## 4.4 如何判断查询慢是慢查询还是索引引起的？

偶尔慢：

- 刷新脏页（刷新脏页时，系统会暂停其他操作）
- 数据被锁住。使用`show processlist` 查看语句执行状态

一直很慢：

- 查询的数据量太大
- 没有用到索引
  - 未建索引
  - 索引失效
  - 系统选错了索引

# 五、事务

## 5.1 数据事务的特性

ACID

Atomic 原子性 undolog

Consistency 一致性 原子性是实现一致性的保障。通常是指中间过程的不可见

Isolation 隔离性 加锁+MVCC

Duration 持久性 redolog+binlog

## 5.2 事务的隔离级别

# 六、数据库三范式

# 七、数据库

## 7.1 你的数据库一会有500个连接数，一会有10个，你分析一下情况

## 7.2 一条sql的执行流程

## 7.3 自增主键id=15，删除15重启，再insert，该记录的id是？

Myisam：把最大id记录到文件中，重启后读取文件insert的id=16

InnoDB：存在内存中，重启后丢失 insert id=15

