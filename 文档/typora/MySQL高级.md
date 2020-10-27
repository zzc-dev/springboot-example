[TOC]

# 一、配置文件

1. 二进制日志log-bin
   主从复制及备份复制。
   log-bin 中存放了所有的操作记录(写？)，可以用于恢复。相当于 Redis 中的 AOF    
   my.cnf中的log-bin配置(默认关闭)
2. 错误日志log-error
   默认是关闭的,记录严重的警告和错误信息，**每次启动和关闭的详细信息等**
3. 慢查询日志log
   默认关闭，记录查询的sql语句，如果开启会减低mysql的整体性能，因为记录日志也是需要消耗系统资源的

# 二、逻辑架构

## 1. 总体概览

<strong style="color:red">插件式的存储引擎架构将查询处理和其它的系统任务以及数据的存储提取相分离。</strong>

这种架构可以根据业务的需求和实际需要选择合适的存储引擎。

![image-20201026104802513](D:\myself\springboot-example\文档\typora\images\mysql01.png)

1.连接层
 最上层是一些客户端和连接服务，包含本地sock通信和大多数基于客户端/服务端工具实现的类似于tcp/ip的通信。主要完成一些类似于连接处理、授权认证、及相关的安全方案。在该层上引入了线程池的概念，为通过认证安全接入的客户端提供线程。同样在该层上可以实现基于SSL的安全链接。服务器也会为安全接入的每个客户端验证它所具有的操作权限。

2.服务层

2.1  Management Serveices & Utilities： 系统管理和控制工具  
2.2  SQL Interface: SQL接口
      接受用户的SQL命令，并且返回用户需要查询的结果。比如select from就是调用SQL Interface
2.3 Parser: 解析器
       SQL命令传递到解析器的时候会被解析器验证和解析。 
2.4 Optimizer: 查询优化器。
     SQL语句在查询之前会使用查询优化器对查询进行优化。 
     用一个例子就可以理解： select uid,name from user where  gender= 1;
     优化器来决定先投影还是先过滤。

2.5 Cache和Buffer： 查询缓存。
      如果查询缓存有命中的查询结果，查询语句就可以直接去查询缓存中取数据。
      这个缓存机制是由一系列小缓存组成的。比如表缓存，记录缓存，key缓存，权限缓存等
       缓存是负责读，缓冲负责写。


3.引擎层
  存储引擎层，存储引擎真正的负责了MySQL中数据的存储和提取，服务器通过API与存储引擎进行通信。不同的存储引擎具有的功能不同，这样我们可以根据自己的实际需要进行选取。后面介绍MyISAM和InnoDB

4.存储层
  数据存储层，主要是将数据存储在运行于裸设备的文件系统之上，并完成与存储引擎的交互。

## 2. 查询说明

<img src="D:\myself\springboot-example\文档\typora\images\mysql02.png" alt="image-20201026105145225" style="zoom:80%;" />

mysql的查询流程大致是：
mysql客户端通过协议与mysql服务器建连接，发送查询语句，先检查查询缓存，如果命中(一模一样的sql才能命中)，直接返回结果，否则进行语句解析,也就是说，在解析查询之前，服务器会先访问查询缓存(query cache)——它存储SELECT语句以及相应的查询结果集。如果某个查询结果已经位于缓存中，服务器就不会再对查询进行解析、优化、以及执行。它仅仅将缓存中的结果返回给用户即可，这将大大提高系统的性能。

语法解析器和预处理：首先mysql通过关键字将SQL语句进行解析，并生成一颗对应的“解析树”。mysql解析器将使用mysql语法规则验证和解析查询；预处理器则根据一些mysql规则进一步检查解析数是否合法。

查询优化器当解析树被认为是合法的了，并且由优化器将其转化成执行计划。一条查询可以有很多种执行方式，最后都返回相同的结果。优化器的作用就是找到这其中最好的执行计划。。

然后，mysql默认使用的BTREE索引，并且一个大致方向是:无论怎么折腾sql，至少在目前来说，<strong style="color:red">mysql最多只用到表中的一个索引。</strong>

## 3. 存储引擎

mysql支持的引擎  show engines
mysql当前默认和使用的引擎  show variables like '%storage_engine%'

| 对比项         | MyISAM                                                   | InnoDB                                                       |
| -------------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| 主外键         | 不支持                                                   | 支持                                                         |
| 事务           | 不支持                                                   | 支持                                                         |
| 行表锁         | 表锁，即使操作一条记录也会锁住整个表，不适合高并发的操作 | 行锁,操作时只锁某一行，不对其它行有影响，适合高并发的操作    |
| 缓存           | 只缓存索引，不缓存真实数据                               | 不仅缓存索引还要缓存真实数据，对内存要求较高，而且内存大小对性能有决定性的影响 |
| 表空间         | 小                                                       | 大                                                           |
| 关注点         | 性能                                                     | 事务                                                         |
| 默认安装       | Y                                                        | Y                                                            |
| 用户表默认使用 | N                                                        | Y                                                            |
| 自带系统表使用 | Y                                                        | N                                                            |

innodb 索引 使用 B+TREE myisam 索引使用 b-tree
innodb 主键为聚簇索引，基于聚簇索引的增删改查效率非常高。

# 三、用户与权限管理

## 1. 用户管理

```
# 创建用户  表示创建名称为zhang3的用户，密码设为123123；
create user zhang3 identified by '123123';

# 查看用户
select host,user,password,select_priv,insert_priv,drop_priv from mysql.user;

# 修改当前用户的密码:
set password =password('123456')
 
# 修改某个用户的密码:
update mysql.user set password=password('123456') where user='li4';
flush privileges;   #所有通过user表的修改，必须用该命令才能生效。

# 删除用户
drop user li4 ;
不要通过delete from  user u where user='li4' 进行删除，系统会有残留信息保留。 
```

## 2. 权限管理

授权命令： 

```
grant 权限1,权限2,…权限n on 数据库名称.表名称 to 用户名@用户地址 identified by ‘连接口令’;
该权限如果发现没有该用户，则会直接新建一个用户。
比如  
grant select,insert,delete,drop on atguigudb.* to li4@localhost  ;
 #给li4用户用本地命令行方式下，授予atguigudb这个库下的所有表的插删改查的权限。

grant all privileges on *.* to joe@'%'  identified by '123'; 
#授予通过网络方式登录的的joe用户 ，对所有库所有表的全部权限，密码设为123.
就算 all privileges 了所有权限，grant_priv 权限也只有 root 才能拥有。

给 root 赋连接口令 grant all privileges on *.* to root@'%'  ;后新建的连接没有密码，需要设置密码才能远程连接。
update user set password=password('root') where user='root' and host='%';
```

收回权限

```
revoke  权限1,权限2,…权限n on 数据库名称.表名称  from  用户名@用户地址 ;
 
REVOKE ALL PRIVILEGES ON mysql.* FROM joe@localhost;
#若赋的全库的表就 收回全库全表的所有权限
 
REVOKE select,insert,update,delete ON mysql.* FROM joe@localhost;
#收回mysql库下的所有表的插删改查权限
 对比赋予权限的方法。
 必须用户重新登录后才能生效
```

查看权限

```
查看当前用户权限
show grants;
 
查看某用户的全局权限
select * from user ;
 
查看某用户的某库的权限
select * from  db;
 
查看某用户的某个表的权限
select * from tables_priv;
```

## 3. 通过工具远程访问

1、先 ping 一下数据库服务器的ip 地址确认网络畅通。

2、关闭数据库服务的防火墙
    systemctl stop wirewalld

3、 确认Mysql中已经有可以通过远程登录的账户
    select  * from mysql.user where user='root' and host='%';

如果没有用户,先执行如下命令：
    grant all privileges on *.*  to li4@'%'  identified by '123123';

修改root用户使用远程连接

```
host字段中，localhost表示只允许本机访问，要实现远程连接，可以将root用户的host改为%，%表示允许任意host访问，如果需要设置只允许特定ip访问，则应改为对应的ip。

use mysql; 
select user, host from user;  # 报次错误，不影响Duplicate entry '%-root' for key 'PRIMARY'
flush privileges;
```

# 四、索引

## 1. SQL执行顺序

手写：

![image-20201026184518073](D:\myself\springboot-example\文档\typora\images\mysql03.png)

机读

![image-20201026184614388](D:\myself\springboot-example\文档\typora\images\mysql04.png)

总结

![image-20201026184643779](D:\myself\springboot-example\文档\typora\images\mysql05.png)

## 2. 索引介绍

### 2.1 是什么

MySQL官方对索引的定义：索引（Index）是帮助Mysql高效获取数据的数据结构
<strong style="color:red">索引是一种数据结构</strong>

你可以简单理解为<strong style='color:red'>“排好序的快速查找数据结构"</strong>

B-TREE: (B:balance)  会自动根据两边的情况自动调节，使两端无限趋近于平衡状态。可以使性能最稳定。(myisam使用的方式)
    B-TREE弊端：(插入/修改操作多时，B-TREE会不断调整平衡，消耗性能)从侧面说明了索引不是越多越好。
B+TREE:Innodb 所使用的索引

一般来说索引本身也很大，不可能全部存储在内存中，因此索引往往以索引文件的形式存储的磁盘上

**我们平常所说的索引，如果没有特别指明，都是指B树(多路搜索树，并不一定是二叉的)结构组织的索引。其中聚集索引，次要索引，覆盖索引，**
**复合索引，前缀索引，唯一索引默认都是使用B+树索引，统称索引。当然，除了B+树这种类型的索引之外，还有哈稀索引(hash index)等。**

### 2.2 优势

类似大学图书馆建书目索引，提高数据检索的效率，降低数据库的IO成本

通过索引列对数据进行排序，降低数据排序的成本，降低了CPU的消耗

### 2.3 劣势

实际上索引也是一张表，该表保存了主键与索引字段，并指向实体表的记录，所以索引列也是要占用空间的

虽然索引大大<strong style="color:red">提高了查询速度，同时却会降低更新表的速度</strong>，如对表进行INSERT、UPDATE和DELETE。
因为更新表时，MySQL不仅要保存数据，还要保存一下索引文件每次更新添加了索引列的字段，
都会调整因为更新所带来的键值变化后的索引信息

索引只是提高效率的一个因素，如果你的MySQL有大数据量的表，就需要花时间研究建立最优秀的索引，或优化查询语句

### 2.4 索引结构

![image-20201026195321119](D:\myself\springboot-example\文档\typora\images\mysql06.png)

### 2.5 索引分类

- **主键索引：**设定为主键后数据库会自动建立索引，innodb为聚簇索引

- **单值索引**：即一个索引只包含单个列，一个表可以有多个单列索引
  除开 innodb 引擎主键默认为聚簇索引 外。 innodb 的索引都采用的 B+TREE
  myisam 则都采用的 B-TREE索引

- **唯一索引**：索引列的值必须唯一，但允许有空值

- **符合索引**：即一个索引包含多个列在数据库操作期间，
  复合索引比单值索引所需要的开销更小(对于相同的多个列建索引)
  当表的行数远大于索引列的数目时可以使用复合索引

  **基本语法**

  ```
  ALTER mytable ADD  [UNIQUE ]  INDEX [indexName] ON (columnname(length)) 
  DROP INDEX [indexName] ON mytable;
  SHOW INDEX FROM table_name
  
  有四种方式来添加数据表的索引：
  ALTER TABLE tbl_name ADD PRIMARY KEY (column_list): 该语句添加一个主键，这意味着索引值必须是唯一的，且不能为NULL。
   
  ALTER TABLE tbl_name ADD UNIQUE index_name (column_list): 这条语句创建索引的值必须是唯一的（除了NULL外，NULL可能会出现多次）。
   
  ALTER TABLE tbl_name ADD INDEX index_name (column_list): 添加普通索引，索引值可出现多次。
   
  ALTER TABLE tbl_name ADD FULLTEXT index_name (column_list):该语句指定了索引为 FULLTEXT ，用于全文索引。
  ```


### 2.6 索引的使用场景

下列情况推荐创建索引：

	1. 主键自动创建唯一索引
 	2. 频繁作为查询条件的字段应该创建索引(where 后面的语句)
 	3. 查询中与其它表关联的字段，外键关系建立索引
     A 表关联 B 表：A join B  。  on 后面的连接条件 既 A 表查询 B 表的条件。所以 B 表被关联的字段建立索引能大大提高查询效率
     因为在 join 中，join 左边的表会用每一个字段去遍历 B 表的所有的关联数据，相当于一个查询操作
 	4. 查询中排序的字段，排序字段若通过索引去访问将大大提高排序速度
 	5. 查询中统计或者分组字段

下列情况不推荐创建索引：

	1. 表记录太少
 	2. 经常增删改的表
 	3. where条件用不到的字段
 	4. 数据重复且分布平均的表字段，因此应该只为最经常查询和最经常排序的数据列建立索引。
     注意，如果某个数据列包含许多重复的内容，为它建立索引就没有太大的实际效果。

## 3.MySQL常见性能瓶颈

1. CPU
   SQL中对大量数据进行比较（最大压力在于比较）、关联、排序、分组
2. IO
   实例内存满足不了缓存数据或者排序等需要，导致产生大量物理IO；
   查询执行效率低，扫描过多数据行
3. 锁
   不适宜的锁设置，导致线程阻塞，性能下降；
   死锁，线程之前交叉调用资源，导致死锁，线程卡住
4. 服务器硬件性能瓶颈
   top、free、iostat和vmstat查看系统的性能状态

# 五、 性能分析之Explain

>使用Explain可以查看执行计划，可以模拟优化器执行SQL查询语句，从而知道mysql是如何处理sql语句的。
>从而分析查询语句和表结构的性能瓶颈

执行计划包含的信息

![image-20201027105500918](D:\myself\springboot-example\文档\typora\images\mysql07.png)

## 1. 字段解释

### 1.1 id

​       id 一致：从上到下顺序执行

​	   id不一致：id越大，优先级越高，越先执行

### 1.2 select_type

查询的类型，主要是用于区别普通查询、联合查询、子查询等的复杂查询

```
1.simple 简单的查询
	explain select * from tab;
	
2.primary 查询中包含子查询，最外层被查询被标记为primary
	explain select * from a where a.id in (selct id from b);   a表为primary b为dependent subquery
	
3.derived 在from中使用子查询生成的临时表标记为derived
	explain select * from (select * from user) a; 

4.subquery 在SELECT或WHERE列表中包含了子查询，子查询基于单值  用 = 
	explain select * from plate where id = (select id from plate where id =1)

5.dependent subquery 在SELECT或WHERE列表中包含了子查询,子查询基于多值  用 in
	explain select * from plate where id in (select id from plate where id =1)

6.uncacheable subquery 无法被缓存的子查询
	explain select * from plate where id = (select id from plate where id = @@sort_buffer_size)
	@@ 表示查的环境参数 。没办法缓存

7.union  所有union的右表，如例子中的a表
8.union result  从union获取整个结果的select
  explain
	select * from plate where id =1
	union
	select * from plate as a where id >1
```

### 1.3 table

### 1.4 type

访问类型排列 性能最好：system > const > eq_ref > ref > range > index > all 

一般来说，查询type至少达到range，最多达到ref

```
1.system 表中只有一条记录
  select * from plate; # plate 只有一条记录

2.const 唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键或唯一索引扫描
  select * from plate where id=1

3.eq_ref 与const的区别，eq_const用于联表查找
  explain select * from plate a,plate b where a.id = b.id;  # b表的type为eq_ref

4.ref 非唯一性索引扫描，返回匹配某个单独值的所有行
  explain select * from staff where deptId = 1; #deptId为普通索引

5.range 只检索给定范围的行,使用一个唯一索引来选择行
  explain select * from staff where staffId > 1; #staffId为主键或唯一索引

6.index 全索引扫描，只遍历索引树
  explain select index_name from table;

7.all 全表扫描
```

### 1.5 possible key 与key

  possible理论上使用的索引，但不一定被实际查询使用
  key实际使用的索引

> 使用覆盖索引： 
> possible为null，而key有值，并且extra为Using Index
>
> 查询的字段和索引重叠：
> eg：explain select staffId from staff ; # staffId为索引

### 1.6 key_len

表示索引中使用的字节数，可通过该列计算查询中使用的索引的长度。

key_len的计算和用处？

![image-20201027165244983](D:\myself\springboot-example\文档\typora\images\mysql08.png)

### 1.7 ref

显示索引的哪一列被使用了，如果可能的话，是一个常数。哪些列或常量被用于查找索引列上的值

```
explain select * from plate a,plate b where a.id = b.id;

table=b
key=primary
ref=test.a.id  
表示b表的主键id索引使用的是test数据库中a表的字段id
```

### 1.8 rows

每张表有多少行被优化器查询
rows列显示MySQL认为它执行查询时必须检查的行数。越少越好

### 1.9 extra

包含不适合在其他列中显示但十分重要的额外信息

```
1.Using filesore 
  mysql会对数据使用一个外部的索引排序，而不是按照表内的索引顺序进行读取。
  MySQL中无法利用索引完成的排序操作称为“文件排序”

2.Using tempporary
  使了用临时表保存中间结果,MySQL在对查询结果排序时使用临时表。常见于排序 order by 和分组查询 group by。
  
3.Using Index和Using where
  表示相应的select操作中使用了覆盖索引(Covering Index)，避免访问了表的数据行，效率不错！
  如果同时出现using where，表明索引被用来执行索引键值的查找;
  如果没有同时出现using where，表明索引只是用来读取数据而非利用索引执行查找。

4.Using join buffer
  使用连接缓存
 
5.impossible where
  where的条件总是false
 
6.select tables optimized away
  Innodb没有该机制
```



# mysql锁机制



# 记忆

## 1.子查询和 join 对比

子查询理解：①先知道需要查询并将数据拿出来(若from 后的表也是一个子查询结果)。②在去寻找满足判断条件的数据(where,on,having 后的参数等)。而这些查询条件通常是通过子查询获得的。
子查询是一种根据结果找条件的倒推的顺序。比较好理解与判断

join理解：执行完第一步后的结果为一张新表。在将新表与 t_emp 进行下一步的 left join 关联。
先推出如何获得条件，再像算数题一样一步一步往下 join。可以交换顺序，但只能是因为条件间不相互关联时才能交换顺序。
join 比 子查询难一点 
<strong style="color:red">join 能用到索引，但是子查询出来的表会使索引失效。</strong>

## 2. MySQL的索引结构为什么使用B+树，而不是其他树形结构？

https://www.bilibili.com/read/cv5985933/