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

- 复合索引**：即一个索引包含多个列在数据库操作期间，
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
1.Using filesort
  mysql会对数据使用一个外部的索引排序，而不是按照表内的索引顺序进行读取。
  MySQL中无法利用索引完成的排序操作称为“文件排序”

2.Using temporary
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
  
7.Using index condition; （5.7新增的）
```

5.7 后新增的

1.10 partitions 

1.11 filtered

## 2. 性能优化

### 2.1 复合索引顺序问题

```
alter table article add index idx_article_ccv(category_id, comments, views);
explain select * from article where category_id = 1 and comments > 1 order by  views limit 1;
建立了ccv符合索引，所以索引是以ccv顺序排序的，
category_id=1 ccv索引是以cv方式排序，后面order by views,与索引顺序不符，所以需要重新排序，etra为using filesort
```

### 2.2  关联索引优化

```
explain select * from class left join book on class.card=book.card;
```

**left join 加右表性能高，right join 加左表性能高**

**驱动表无论如何都会被全表扫描。所以扫描次数越少越好**

1.被驱动表  join 后的表为被驱动表  (需要被查询)

2.left join 尽量选择小表作为驱动表，小表驱动大表

3.inner join mysql 自动选择。小表作为驱动表。

4.尽量不在被驱动表中使用子查询，有可能使用不到索引

### 2.3 索引失效

​	1. <strong style="color:red">最佳左前缀法则：查询从索引的最左前列开始并且不跳过索引中间的列</strong>

```
alter table1 add index idxABC(A,B,C);

explain select * from table1 where A = 1 and C=1; #可以使用A索引，C失效，违背最佳左前缀法则
explain select * from table1 where B = 1 and C=1; #不能使用索引，违背最佳左前缀法则
```

2. 不要在索引列上做任何操作（计算、函数、手动或自动类型转换），会使索引失效从而转向全表扫描

3. 存储引擎不能使用索引中<strong style="color:red">范围条件右边</strong>的列

   ```
   alter table1 add index idxABC(A,B,C);
   
   explain select * from table1 where A = 1 and B>1 and C=1; #可以使用AB索引，C失效，type=range
   ```

4. 尽量使用覆盖索引，但使用覆盖索引后<strong style="color:red">范围及范围后</strong>也全部失效

   ```
   alter table1 add index idxABC(A,B,C);
   
   # 可以使用AB索引，C失效，type=range
   explain select * from table1 where A = 1 and B>1 and C=1;
   
   #可以使用A索引，BC失效，type=ref  extra=using index
   explain select A,B,C from table1 where A = 1 and B>1 and C=1; 
   ```

5. mysql使用“!=”、“<>”、is null、is not null 会使索引失效导致全表扫描

6. like 左边使索引失效，可以使用覆盖索引解决

   ```
   alter table1 add index idxA(A);
   
   # like左边有%，索引失效
   explain select * from table1 where A like '%s%';
   explain select * from table1 where A like '%s';
   
   # type=index，extra=using index   索引不失效
   explain select A from table1 where A like '%s';
   ```

   6.1 **复合索引下like, ‘<’ 范围的对比**

   like 常量开头就会使用到索引并且不会影响复合索引后面的使用

   | alter table1 add index idxABC(A,B,C);     | A    | B    | C    |
   | :---------------------------------------- | ---- | ---- | ---- |
   | where A='s' and B like '%s' and C = 's'   | √    | ×    | ×    |
   | where A='s' and B like 's%' and C = 's'   | √    | √    | √    |
   | where A='s' and B like 's%s%' and C = 's' | √    | √    | √    |
   | where A='s' and B > 's' and C = 's'       | √    | √    | ×    |

   

7. 字符串不加单引号会使索引失效，自动类型转换了

8. or也会使索引失效，与like一样，可以使用覆盖索引解决

9. order by

   ```
   alter table1 add index idxABC(A,B,C);
   # 使用到了A用于查找，B用于排序，C失效
   explain select * from table1 where A = 1 and C=1 order by B;
   ```

10. group by 先排序后分组，如果出现系统内排序，将会产生临时表，严重影响性能

    ```
    alter table1 add index idxABC(A,B,C);
    # type=ref,使用了聚合索引中的A，extra=Using temporary; Using filesort
    explain select * from table1 where A = 1 group by C;
    ```

### 2.4 【2.3总结】

​                          **全值匹配我最爱，最左前缀要遵守。**
​                          **带头大哥不能死，中间兄弟不能断。**
​                          **索引列上少计算，范围之后全失效。**
​                          **Like百分写最右，覆盖索引不写*。**
​                          **不等空值还有or，索引失效要少用。**

# 六、查询优化

​	1.慢查询的开启并捕获
​    2.explain+慢SQL分析
​    3.show profiles查询SQL在服务器中的执行细节和生命周期情况
​    4.SQL数据库服务器的参数调优

## 1. in和exists的对比

```
select * from A where id in (select B.id from B)
查询结果上等价于
select * from A where exists (select 1 from B where B.id = A.id)
```

上面两条查询语句从结果上是一致的，但执行顺序不一样

in是先开始子查询，也就是先B后A

exists

> 指定一个子查询，检测行的存在。遍历循环外表，然后看外表中的记录有没有和内表的数据一样的。匹配上就将结果放入结果集中

根据exist的定义，执行顺序是A -> B

总结：

​	根据小表驱动大表的原则，使用in：内表数据量<外表，exists相反

## 2. order by

MySQL支持二种方式的排序，FileSort和Index，Index效率高.
它指MySQL扫描索引本身完成排序。[FileSort](#FileSort)方式效率较低

## 3. group by

- group by实质是先排序后进行分组，遵照索引建的最佳左前缀 
- 当无法使用索引列，增大max_length_for_sort_data参数的设置+增大sort_buffer_size参数的设置
- where高于having，能写在where限定的条件就不要去having限定了。

## 4. distinct

```
例子：select kcdz form t_mall_sku where id in( 3,4,5,6,8 )  将产生重复数据，
     select distinct kcdz form t_mall_sku where id in( 3,4,5,6,8 )   使用 distinct 关键字去重消耗性能
优化： select  kcdz form t_mall_sku where id in( 3,4,5,6,8 )  group by kcdz 能够利用到索引
```



## FileSort

如果不在索引列上，filesort有两种算法：
mysql就要启动双路排序和单路排序

>双路排序：MySQL 4.1从磁盘取排序字段，在buffer进行排序，再从磁盘取其他字段。
>                   两次磁盘扫描，IO耗时
>
>单路排序：从磁盘查询所有的列，在buffer中排序输出
>                   一次IO，但数据一次性读取在内存中，占据更多空间
>           可能出现问题：
>                   一次读取的数据超出sort_buffer的容量，导致每次只能取sort_buffer容量大小的数据，进行排序（创建tmp文件，多路合并），排完再取取 
>                  sort_buffer容量大小，再排……从而多次I/O。
>          优化策略：
>				1.增大sort_buffer_size参数的设置
>                2.增大max_length_for_sort_data参数的设置
>                3.去掉select 后面不需要的字段



# 七、查询截取分析

## 1. 慢查询日志

>MySQL的慢查询日志是MySQL提供的一种日志记录，它用来记录在MySQL中响应时间超过阀值的语句，具体指运行时间超过long_query_time值的SQL，则会被记录到慢查询日志中
>
>默认情况下，MySQL数据库没有开启慢查询日志
>
>如果不是调优需要的话，一般不建议启动该参数，因为开启慢查询日志会或多或少带来一定的性能影响。慢查询日志支持将日志记录写入文件

### 1.1**相关命令**

```
#查看慢查询是否开启和日志存放位置
SHOW VARIABLES LIKE '%slow_query_log%';

#开启慢查询 mysql重启后失效
set global slow_query_log=1

#查看超时时间 
SHOW VARIABLES LIKE 'long_query_time%';
#要重新连接或新开一个会话才能看到修改值
set global long_query_time=1

#测试
select sleep(11);

#查看系统中有多少条慢查询日志
show global status like '%Slow_queries%';

【配置版】
【mysqld】下配置：
 slow_query_log=1;
slow_query_log_file=/var/lib/mysql/atguigu-slow.log
long_query_time=3;
log_output=FILE
```

### 1.2 慢查询日志分析工具mysqldumpslow

![image-20201028190930672](D:\myself\springboot-example\文档\typora\images\mysql09.png)

常用命令：

```
得到返回记录集最多的10个SQL
mysqldumpslow -s r -t 10 /var/lib/mysql/atguigu-slow.log
 
得到访问次数最多的10个SQL
mysqldumpslow -s c -t 10 /var/lib/mysql/atguigu-slow.log
 
得到按照时间排序的前10条里面含有左连接的查询语句
mysqldumpslow -s t -t 10 -g "left join" /var/lib/mysql/atguigu-slow.log
 
另外建议在使用这些命令时结合 | 和more 使用 ，否则有可能出现爆屏情况
mysqldumpslow -s r -t 10 /var/lib/mysql/atguigu-slow.log | more
```

## 2. show profile

> 是mysql提供可以用来分析当前会话中语句执行的资源消耗情况。可以用于SQL的调优的测量
>
> 默认情况下，参数处于关闭状态，并保存最近15次的运行结果

### **命令**

```
Show  variables like 'profiling'; # 查看是否开启
set profiling=1; # 开启
```

```
show profiles; #查看查询结果
```

![image-20201028195554900](D:\myself\springboot-example\文档\typora\images\mysql10.png)

```
show profile cpu,block io for query  n #诊断SQL，n为上一步前面的问题SQL数字号码;

相关参数:  
 | ALL                        --显示所有的开销信息  
 | BLOCK IO                --显示块IO相关开销  
 | CONTEXT SWITCHES --上下文切换相关开销  
 | CPU              --显示CPU相关开销信息  
 | IPC              --显示发送和接收相关开销信息  
 | MEMORY           --显示内存相关开销信息  
 | PAGE FAULTS      --显示页面错误相关开销信息  
 | SOURCE           --显示和Source_function，Source_file，Source_line相关的开销信息  
 | SWAPS            --显示交换次数相关开销的信息

```

![image-20201028195806542](D:\myself\springboot-example\文档\typora\images\mysql11.png)

### status如下状态需要注意

            1. converting HEAP to MyISAM 查询结果太大，内存都不够用了往磁盘上搬了。
               2. Creating tmp table 创建临时表
               3. Copying to tmp table on disk 把内存中临时表复制到磁盘，危险！！
               4. locked

## 3.全局日志查询

<strong style="color:red">尽量不要在生产环境开启这个功能。</strong>

```
配置启用：
	在mysql的my.cnf中，设置如下：
	#开启
	general_log=1   
	# 记录日志文件的路径
	general_log_file=/path/logfile
	#输出格式
	log_output=FILE

编码启动：
	set global general_log=1;
 	#全局日志可以存放到日志文件中，也可以存放到Mysql系统表中。存放到日志中性能更好一些，存储到表中
	set global log_output='TABLE';
 
 	此后 ，你所编写的sql语句，将会记录到mysql库里的general_log表，可以用下面的命令查看
 	select * from mysql.general_log;
```



# 八、mysql锁机制

>  锁是计算机协调多个进程或线程并发访问某一资源的机制

## 1.分类

按对数据的操作类型分：
		读锁（共享锁）：针对同一份数据，多个读操作可以同时进行不会互相影响
	    写锁（排他锁）：当前写操作没有完成前，会阻塞其他的读和写操作
                    当使用lock锁表时，阻塞其他的读和写操作
                    当使用for update或update时，只会阻塞写操作	

对数据操作的粒度分：

		 > 为了尽可能提高数据库的并发度，每次锁定的数据范围越小越好，理论上每次只锁定当前操作的数据的方案会得到最大的并发度，但是管理锁是很耗资源的事情（涉及获取，检查，释放锁等动作），因此数据库系统需要在高并发响应和系统性能两方面进行平衡，这样就产生了“锁粒度（Lock granularity）”的概念。

​         表锁
​	     行锁

## 2. 表锁(偏读)

偏向MyISAM存储引擎，开销小，加锁快；无死锁；锁定粒度大，发生锁冲突的概率最高,并发度最低。

```
# 查看表上加过的锁
show open tables [from dbname];
#添加锁
lock tables tablename read/write [, tablename2 read/write ..];
#释放锁
unlock tables;
```

**读锁**

​                                      session1             session2
​                                        加读锁
   查询该表                        Y                           Y
   查询其他表                    N                          Y
   更新改表                        N                         阻塞
​                                         释放锁                 更新成功

**写锁** 

​                                      session1             session2                                    
​                                        加写锁

   查询该表                        Y                        阻塞
   查询其他表                    N                          Y
   更新改表                        Y                         阻塞
                                         释放锁                 更新成功/查询成功

   <strong style="color:red">读锁会阻塞写，但是不会堵塞读。而写锁则会把读和写都堵塞   </strong>                                                         

## 3. 行锁（偏写）

偏向InnoDB存储引擎，开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低,并发度也最高。

InnoDB与MyISAM的最大不同有两点：一是支持事务（TRANSACTION）；二是采用了行级锁           

​                                        session1                                   session2                                    
​                                set autocommit = 0                   set autocommit = 0

​                                 更新改行但未提交                        查询到之前的数据（防止脏读）、更新阻塞                               
​                                       commit                                   查询到之前的数据（防止不可重复读）、可以更新
​                                                                                         commit;  查到session1更新的数据

   <strong style="color:red">无索引行锁升级为表锁  </strong>    

### 行锁分析

```
show status like 'innodb_row_lock%';# 分析系统上的行锁的争夺情况

Innodb_row_lock_current_waits：当前正在等待锁定的数量；
Innodb_row_lock_time：从系统启动到现在锁定总时间长度；
Innodb_row_lock_time_avg：每次等待所花平均时间；
Innodb_row_lock_time_max：从系统启动到现在等待最常的一次所花的时间；
Innodb_row_lock_waits：系统启动后到现在总共等待的次数；
对于这5个状态变量，比较重要的主要是
  Innodb_row_lock_time_avg（等待平均时长），
  Innodb_row_lock_waits（等待总次数）
  Innodb_row_lock_time（等待总时长）这三项。

#查询正在被锁阻塞的sql语句。
SELECT * FROM information_schema.INNODB_TRX\G;
```

### 优化建议

- 尽可能让所有数据检索都通过索引来完成，避免无索引行锁升级为表锁
- 尽可能较少检索条件，避免间隙锁
- 尽量控制事务大小，减少锁定资源量和时间长度
- 锁住某行后，尽量不要去调别的行或表，赶紧处理被锁住的行然后释放掉锁。
- 涉及相同表的事务，对于调用表的顺序尽量保持一致。
- 在业务环境允许的情况下,尽可能低级别事务隔离

## 4. select加锁

```
select ..lock in share mode; #读锁
select... for update; # 写锁
```

## 5. 间隙锁

**幻读**：一个事务(同一个read view)在前后两次查询同一范围的时候，后一次查询看到了前一次查询没有看到的行

innodb默认可重复读（RR）下，普通查询（快照读）是看不到其他事务提交的更改
**幻读只发生在当前读**	

 **如何解决幻读**

- 将两行记录间的空隙加上锁，阻止新记录的插入；这个锁称为**间隙锁**。
- 间隙锁与间隙锁之间没有冲突关系。跟间隙锁存在冲突关系的，是**往这个间隙中插入一个记录**这个操作。

**间隙锁的生成条件**

​        1.对主键或唯一索引，如果当前读时，where条件全部精确命中(=或者in)，这种场景本身就不会出现幻读，所以只会加行记录锁。

　　2.没有索引的列，当前读操作时，会加全表gap锁，生产环境要注意。

　　3.非唯一索引列，如果where条件部分命中(>、<、like等)或者全未命中，则会加附近Gap间隙锁。例如，某表数据如下，非唯一索引2,6,9,9,11,15。如下语句要操作非唯一索引列9的数据，gap锁将会锁定的列是(6,11]，该区间内无法插入数据。

## 6. 页锁

开销和加锁时间界于表锁和行锁之间；会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般。

# 九、主从复制

## 1. 原理

![image-20201029100327161](D:\myself\springboot-example\文档\typora\images\mysql12.png)

>1 master将改变记录到二进制日志（binary log）。这些记录过程叫做二进制日志事件，binary log events；
>2 slave将master的binary log events拷贝到它的中继日志（relay log）；
>3 slave重做中继日志中的事件，将改变应用到自己的数据库中。 MySQL复制是异步的且串行化的
>
>复制最大的问题是延时

## 2. 配置

以windows为主机，linux为从机为例

### 2.1 主机修改my.ini

```
[mysqld]
service-id=1                                 # 1. 【必须】主服务器唯一id
log-bin=自己本地的路径/data/mysqlbin            # 2. 【必须】启用二进制日志文件
log-err=自己本地的路径/data/mysqlerr            # 3. 【可选】启动错误日志
basedir="自己本地路径"                          # 4. 【可选】根目录
tmpdir="自己本地路径"                           # 5. 【可选】临时目录
datadir="自己本地路径/Data/"                    # 6. 【可选】数据目录
read-only=0                                   # 7.【必须】主机读写都可以
binlog-ignore-db=mysql                        # 8.【可选】设置不要复制的数据库
binlog-do-db=需要复制的主数据库名字               # 9.【可选】设置需要复制的数据库，不选默认复制全部
```

### 2.2 从机修改my.cnf

```
[mysqld]
service-id=2                                 # 1. 【必须】从机服务器唯一id
```

### 2.3 其他操作

​        修改了配置文件，重启mysql
​        关闭linux和windows的防火墙

### 2.4  master建立账户并授权slave

```
GRANT REPLICATION SLAVE ON *.* TO 'zhangsan'@'从机器数据库IP' IDENTIFIED BY '123456';
flush privileges;
```

```
show master status;
```

![image-20201029102149628](D:\myself\springboot-example\文档\typora\images\mysql13.png)

**记录下File和Position的值**
**执行完此步骤后不要再操作主服务器MYSQL，防止主服务器状态值变化**

### 2.5 slave配置需要复制的master

```
CHANGE MASTER TO MASTER_HOST='192.168.124.3',
MASTER_USER='zhangsan',
MASTER_PASSWORD='123456',
MASTER_LOG_FILE='mysqlbin.具体数字',MASTER_LOG_POS=具体值;

start/stop slave; # 启动/停止从服务器复制功能

# 验证主从配置成功  Slave_IO_Running: Yes   Slave_SQL_Running: Yes
show slave status\G   
```

# 概念

## 1. 当前读和快照读

- ### 当前读

　　select...lock in share mode (共享读锁)
　　select...for update
　　update , delete , insert

　　当前读, 读取的是最新版本, 并且**对读取的记录加锁, 阻塞其他事务同时改动相同****记录****，避免出现安全问题**。

　　例如，假设要update一条记录，但是另一个事务已经delete这条数据并且commit了，如果不加锁就会产生冲突。所以update的时候肯定要是当前读，得到最新的信息并且锁定相应的记录。

​        **当前读的实现方式：next-key锁(行记录锁+Gap间隙锁)**

- ###  快照读

　　单纯的select操作，**不包括**上述 select ... lock in share mode, select ... for update。　　　　

　　Read Committed隔离级别：每次select都生成一个快照读。

　　Read Repeatable隔离级别：**开启事务后第一个select语句才是快照读的地方，而不是一开启事务就快照读。**

​        **快照读的实现方式：undolog和多版本并发控制MVCC**

## 2. 聚簇索引和非聚簇索引

**聚簇索引**：innodb的主键索引，叶子节点保存着数据，二级索引指向主键索引

​			1: 主键索引 既存储索引值,又在叶子中存储行的数据

　　    2: 如果没有主键, 则会Unique key做主键

　　    3: 如果没有unique,则系统生成一个内部的rowid做主键.

　　    4: 像innodb中,主键的索引结构中,既存储了主键值,又存储了行数据,这种结构称为”聚簇索引”

​    优势：

​          一个索引项直接对应实际数据记录的存储页，可谓“直达”

​         索引项的排序和数据行的存储排序完全一致，利用这一点，想修改数据的存储顺序，可以通过改变主键的方法

​    劣势：

​         采用平衡二叉树算法，不规则的数据插入，会改变之前的节点状态，导致页分裂问题。

**非聚簇索引**：myisam的主键索引，二级索引和主键索引的叶子节点都有一个指针指向物理行

## 3. 回表和覆盖索引

![img](D:\myself\springboot-example\文档\typora\images\mysql14.png)

  回表查询：扫描了两次索引树

（1）先通过普通索引定位到主键值；
（2）在通过聚簇索引定位到行记录；

  覆盖索引：extra：Using Index

​         直接通过聚簇索引定位到行记录

## 4. B树和B+树

https://www.cnblogs.com/kenD/p/12751177.html

B树：

​	所有节点都存放数据，由于每页的存储范围有限，当data比较大时会导致每个节点存储的key数量下降，

​    key数量变小回导致树的深度变大，增加查询的磁盘io，继而影响查询性能

![image-20201029185855324](D:\myself\springboot-example\文档\typora\images\mysql15.png)





![image-20201029190900648](D:\myself\springboot-example\文档\typora\images\mysql16.png)

![image-20201029192551391](D:\myself\springboot-example\文档\typora\images\mysql17.png)

# 记忆

## 1.子查询和 join 对比

子查询虽然很灵活，但是执行效率并不高，原因：
执行子查询时，MySQL需要创建临时表，查询完毕后再删除这些临时表，所以，子查询的速度会受到一些影响，这里多了一个创建和销毁临时表的过程。
<strong style="color:red">join 能用到索引，但是子查询出来的表有时会使索引失效。</strong>

## 2. MySQL的索引结构为什么使用B+树，而不是其他树形结构？

https://www.bilibili.com/read/cv5985933/