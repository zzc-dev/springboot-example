[TOC]

# 本单元目标
​	一、为什么要学习数据库
​	二、数据库的相关概念      
​		DBMS、DB、SQL
​	三、数据库存储数据的特点
​	四、初始MySQL
​		MySQL产品的介绍        
​		MySQL产品的安装          ★        
​		MySQL服务的启动和停止     ★
​		MySQL服务的登录和退出     ★      
​		MySQL的常见命令和语法规范      
​	五、DQL语言的学习   ★              
​		基础查询        ★             
​		条件查询  	   ★			
​		排序查询  	   ★				
​		常见函数        ★               
​		分组函数        ★              
​		分组查询		   ★			
​		连接查询	 	★			
​		子查询       √                  
​		分页查询       ★              
​		union联合查询	√			
​		

```gaoji
六、DML语言的学习    ★             
	插入语句						
	修改语句						
	删除语句						
七、DDL语言的学习  
	库和表的管理	 √				
	常见数据类型介绍  √          
	常见约束  	  √			
八、TCL语言的学习
	事务和事务处理                 
九、视图的讲解           √
十、变量                      
十一、存储过程和函数   
十二、流程控制结构       
```

# 前瞻

## 1. 数据库的好处
​	1.持久化数据到本地
​	2.可以实现结构化查询，方便管理
​	

## 2.数据库相关概念
​	1、DB：数据库，保存一组有组织的数据的容器
​	2、DBMS：数据库管理系统，又称为数据库软件（产品），用于管理DB中的数据
​	3、SQL:结构化查询语言，用于和DBMS通信的语言

## 3.数据库存储数据的特点
​	1、将数据放到表中，表再放到库中
​	2、一个数据库中可以有多个表，每个表都有一个的名字，用来标识自己。表名具有唯一性。
​	3、表具有一些特性，这些特性定义了数据在表中如何存储，类似java中 “类”的设计。
​	4、表由列组成，我们也称为字段。所有表都是由一个或多个列组成的，每一列类似java 中的”属性”
​	5、表中的数据是按行存储的，每一行类似于java中的“对象”。



# MySQL产品的介绍和安装

## 1.MySQL服务的启动和停止
​	方式一：计算机——右击管理——服务
​	方式二：通过管理员身份运行
​	net start 服务名（启动服务）
​	net stop 服务名（停止服务）

## 2.MySQL服务的登录和退出   
​	方式一：通过mysql自带的客户端
​	只限于root用户

	方式二：通过windows自带的客户端
	登录：
	mysql 【-h主机名 -P端口号 】-u用户名 -p密码
	
	退出：
	exit或ctrl+C


## 3.MySQL的常见命令 

	1.查看当前所有的数据库
	show databases;
	2.打开指定的库
	use 库名
	3.查看当前库的所有表
	show tables;
	4.查看其它库的所有表
	show tables from 库名;
	5.创建表
	create table 表名(
	
		列名 列类型,
		列名 列类型，
		。。。
	);
	6.查看表结构
	desc 表名;


	7.查看服务器的版本
	方式一：登录到mysql服务端
	select version();
	方式二：没有登录到mysql服务端
	mysql --version
	或
	mysql --V



## 4.MySQL的语法规范
​	1.不区分大小写,但建议关键字大写，表名、列名小写
​	2.每条命令最好用分号结尾
​	3.每条命令根据需要，可以进行缩进 或换行
​	4.注释
​		单行注释：#注释文字
​		单行注释：-- 注释文字
​		多行注释：/* 注释文字  */

## 5.SQL的语言分类
​	DQL（Data Query Language）：数据查询语言
​		select 
​	DML(Data Manipulate Language):数据操作语言
​		insert 、update、delete
​	DDL（Data Define Languge）：数据定义语言
​		create、drop、alter
​	TCL（Transaction Control Language）：事务控制语言
​		commit、rollback

## 6.SQL的常见命令

	show databases； 查看所有的数据库
	use 库名； 打开指定 的库
	show tables ; 显示库中的所有表
	show tables from 库名;显示指定库中的所有表
	create table 表名(
		字段名 字段类型,	
		字段名 字段类型
	); 创建表
	
	desc 表名; 查看指定表的结构
	select * from 表名;显示表中的所有数据

# DQL

## 1. 进阶1：基础查询

​	语法：
​	SELECT 要查询的东西
​	【FROM 表名】;

	类似于Java中 :System.out.println(要打印的东西);
	特点：
	①通过select查询完的结果 ，是一个虚拟的表格，不是真实存在
	② 要查询的东西 可以是常量值、可以是表达式、可以是字段、可以是函数

## 2. 进阶2：条件查询
​	条件查询：根据条件过滤原始表的数据，查询到想要的数据
​	语法：
​	select 
​		要查询的字段|表达式|常量值 |函数
​	from 
​		表
​	where 
​		条件 ;

	分类：
	一、条件表达式
		示例：salary>10000
		条件运算符：
		> < >= <= = != <>
	
	二、逻辑表达式
	示例：salary>10000 && salary<20000
	
	逻辑运算符：
	
		and（&&）:两个条件如果同时成立，结果为true，否则为false
		or(||)：两个条件只要有一个成立，结果为true，否则为false
		not(!)：如果条件成立，则not后为false，否则为true
	
	三、模糊查询
	示例：last_name like 'a%'

## 3. 进阶3：排序查询	

	语法：
	select
		要查询的东西
	from
		表
	where 
		条件
	
	order by 排序的字段|表达式|函数|别名 【asc|desc】


## 4. 进阶4：常见函数
###  4.1、单行函数

#### 	1、字符函数

​		concat 拼接
​		substr 截取子串
​		upper 转换成大写
​		lower 转换成小写
​		trim 去前后指定的空格和字符
​		ltrim 去左边空格
​		rtrim 去右边空格
​		replace 替换
​		lpad 左填充
​		rpad 右填充
​		instr 返回子串第一次出现的索引
​		length  获取<strong style="color:red">字节个数</strong>

> utf8编码：中文占3个字节；gbk编码，中文占2个字节

#### 2、数学函数

	    now当前系统日期+时间
		curdate当前系统日期
		curtime当前系统时间
		str_to_date 将字符转换成日期
		date_format将日期转换成字符

#### 3、日期函数

```
    round 四舍五入
	rand 随机数
	floor向下取整
	ceil向上取整
	mod取余
	truncate截断
```

#### 4、流程控制函数

```
#1.if函数： if else 的效果

SELECT IF(10<5,'大','小');

SELECT last_name,commission_pct,IF(commission_pct IS NULL,'没奖金，呵呵','有奖金，嘻嘻') 备注
FROM employees;




#2.case函数的使用一： switch case 的效果

/*
java中
switch(变量或表达式){
	case 常量1：语句1;break;
	...
	default:语句n;break;


}

mysql中

case 要判断的字段或表达式
when 常量1 then 要显示的值1或语句1;
when 常量2 then 要显示的值2或语句2;
...
else 要显示的值n或语句n;
end
*/

/*案例：查询员工的工资，要求

部门号=30，显示的工资为1.1倍
部门号=40，显示的工资为1.2倍
部门号=50，显示的工资为1.3倍
其他部门，显示的工资为原工资

*/


SELECT salary 原始工资,department_id,
CASE department_id
WHEN 30 THEN salary*1.1
WHEN 40 THEN salary*1.2
WHEN 50 THEN salary*1.3
ELSE salary
END AS 新工资
FROM employees;



#3.case 函数的使用二：类似于 多重if
/*
java中：
if(条件1){
	语句1；
}else if(条件2){
	语句2；
}
...
else{
	语句n;
}

mysql中：

case 
when 条件1 then 要显示的值1或语句1
when 条件2 then 要显示的值2或语句2
。。。
else 要显示的值n或语句n
end
*/

#案例：查询员工的工资的情况
如果工资>20000,显示A级别
如果工资>15000,显示B级别
如果工资>10000，显示C级别
否则，显示D级别


SELECT salary,
CASE 
WHEN salary>20000 THEN 'A'
WHEN salary>15000 THEN 'B'
WHEN salary>10000 THEN 'C'
ELSE 'D'
END AS 工资级别
FROM employees;
```

#### 5、其他函数

​	version 版本
​	database 当前库
​	user 当前连接用户

### 4.2、分组函数


		sum 求和
		max 最大值
		min 最小值
		avg 平均值
		count 计数
	
		特点：
		1、以上五个分组函数都忽略null值，除了count(*)
		2、sum和avg一般用于处理数值型
			max、min、count可以处理任何数据类型
	    3、都可以搭配distinct使用，用于统计去重后的结果
		4、count的参数可以支持：
			字段、*、常量值，一般放1
	
		   建议使用 count(*)

## 5. 进阶5：分组查询
​	语法：
​	select 查询的字段，分组函数
​	from 表
​	group by 分组的字段

功能：用作统计使用，又称为聚合函数或统计函数或组函数

分类：
sum 求和、avg 平均值、max 最大值 、min 最小值 、count 计算个数

特点：
1、sum、avg一般用于处理数值型
   max、min、count可以处理任何类型
2、以上分组函数都忽略null值

3、可以和distinct搭配实现去重的运算

4、count函数的单独介绍
一般使用count(*)用作统计行数

5、和分组函数一同查询的字段要求是group by后的字段

	特点：
	1、可以按单个字段分组
	2、和分组函数一同查询的字段最好是分组后的字段
	3、分组筛选
			针对的表	位置			关键字
	分组前筛选：	原始表		group by的前面		where
	分组后筛选：	分组后的结果集	group by的后面		having
	
	4、可以按多个字段分组，字段之间用逗号隔开
	5、可以支持排序
	6、having后可以支持别名

## 6. 进阶6：多表连接查询

	笛卡尔乘积：如果连接条件省略或无效则会出现
	解决办法：添加上连接条件
	按功能分类：
			内连接：
				等值连接 select * from A,B where A.id = B.id
				非等值连接 select * from A,B where A.num > B.count
				自连接 select * from A a1,A a2 where a1.id = a2.pid
			外连接：
				左外连接
				右外连接
				全外连接
			
			交叉连接

一、传统模式下的连接 ：等值连接——非等值连接


	1.等值连接的结果 = 多个表的交集
	2.n表连接，至少需要n-1个连接条件
	3.多个表不分主次，没有顺序要求
	4.一般为表起别名，提高阅读性和性能

二、sql99语法：通过join关键字实现连接

	含义：1999年推出的sql语法
	支持：
	等值连接、非等值连接 （内连接）
	外连接
	交叉连接
	
	语法：
	
	select 字段，...
	from 表1
	【inner|left outer|right outer|cross】join 表2 on  连接条件
	【inner|left outer|right outer|cross】join 表3 on  连接条件
	【where 筛选条件】
	【group by 分组字段】
	【having 分组后的筛选条件】
	【order by 排序的字段或表达式】
	
	好处：语句上，连接条件和筛选条件实现了分离，简洁明了！


三、自连接

案例：查询员工名和直接上级的名称

sql99

	SELECT e.last_name,m.last_name
	FROM employees e
	JOIN employees m ON e.`manager_id`=m.`employee_id`;

sql92


	SELECT e.last_name,m.last_name
	FROM employees e,employees m 
	WHERE e.`manager_id`=m.`employee_id`;

## 7. 进阶7：子查询

含义：

	一条查询语句中又嵌套了另一条完整的select语句，其中被嵌套的select语句，称为子查询或内查询
	在外面的查询语句，称为主查询或外查询

特点：

	1、子查询都放在小括号内
	2、子查询可以放在from后面、select后面、where后面、having后面，但一般放在条件的右侧
	3、子查询优先于主查询执行，主查询使用了子查询的执行结果
	4、子查询根据查询结果的行数不同分为以下两类：
	① 单行子查询
		结果集只有一行
		一般搭配单行操作符使用：> < = <> >= <= 
		非法使用子查询的情况：
		a、子查询的结果为一组值
		b、子查询的结果为空
		
	② 多行子查询
		结果集有多行
		一般搭配多行操作符使用：any、all、in、not in
		in： 属于子查询结果中的任意一个就行
		any和all往往可以用其他查询代替

分类：	
```
按子查询出现的位置：
select后面：
	仅仅支持标量子查询
from后面：
	支持表子查询
where或having后面：★
	标量子查询（单行） √
	列子查询  （多行） √
	
	行子查询
	
exists后面（相关子查询）
	表子查询
	
按结果集的行列数不同：
标量子查询（结果集只有一行一列）
列子查询（结果集只有一列多行）
行子查询（结果集有一行多列）
表子查询（结果集一般为多行多列）
```


## 8. 进阶8：分页查询

应用场景：

	实际的web项目中需要根据用户的需求提交对应的分页查询的sql语句

语法：

	select 字段|表达式,...
	from 表
	【where 条件】
	【group by 分组字段】
	【having 条件】
	【order by 排序的字段】
	limit 【起始的条目索引，】条目数;

特点：

	1.起始条目索引从0开始
	
	2.limit子句放在查询语句的最后
	
	3.公式：select * from  表 limit （page-1）*sizePerPage,sizePerPage
	假如:
	每页显示条目数sizePerPage
	要显示的页数 page

## 9. 进阶9：联合查询

引入：
	union 联合、合并

语法：

	select 字段|常量|表达式|函数 【from 表】 【where 条件】 union 【all】
	select 字段|常量|表达式|函数 【from 表】 【where 条件】 union 【all】
	select 字段|常量|表达式|函数 【from 表】 【where 条件】 union  【all】
	.....
	select 字段|常量|表达式|函数 【from 表】 【where 条件】

特点：

	1、多条查询语句的查询的列数必须是一致的
	2、多条查询语句的查询的列的类型几乎相同
	3、union代表去重，union all代表不去重

# DML语言

数据操作语言：
	插入：insert
	修改：update
	删除：delete

## 1. 插入

语法：

```
方式1：
	insert into 表名(字段名，...)
	values(值1，...);

方式2：
	insert into 表名
	set 列名=值,列名=值,...

比较：
     1 .方式一支持插入多行,方式二不支持
     2. 方式一支持子查询，方式二不支持

INSERT INTO beauty(id,NAME,phone)
SELECT 26,'宋茜','11809866';

INSERT INTO beauty(id,NAME,phone)
SELECT id,boyname,'1234567'
FROM boys WHERE id<3;
```

特点：

	1、字段类型和值类型一致或兼容，而且一一对应
	2、可以为空的字段，可以不用插入值，或用null填充
	3、不可以为空的字段，必须插入值
	4、字段个数和值的个数必须一致
	5、字段可以省略，但默认所有字段，并且顺序和表中的存储顺序一致

## 2. 修改

修改单表语法：

	update 表名 set 字段=新值,字段=新值
	【where 条件】
修改多表语法：

	update 表1 别名1,表2 别名2
	set 字段=新值，字段=新值
	where 连接条件
	and 筛选条件

## 3. 删除

方式1：delete语句 

单表的删除： ★
	delete from 表名 【where 筛选条件】

多表的删除：
	delete 别名1，别名2
	from 表1 别名1，表2 别名2
	where 连接条件
	and 筛选条件;


方式2：truncate语句

	truncate table 表名

<strong style="color:red">  两种方式的区别【面试题】</strong>	

	#1.truncate不能加where条件，而delete可以加where条件
	
	#2.truncate的效率高一丢丢
	
	#3.truncate 删除带自增长的列的表后，如果再插入数据，数据从1开始
	#delete 删除带自增长列的表后，如果再插入数据，数据从上一次的断点处开始
	
	#4.truncate删除不能回滚，delete删除可以回滚

# DDL语句
数据定义语言

## 1. 库和表的管理

库的管理：

	一、创建库
	create database 库名
	二、删除库
	drop database 库名
	三、更改库的字符集
	ALTER DATABASE books CHARACTER SET gbk;
表的管理：

	一、创建表
	CREATE TABLE IF NOT EXISTS stuinfo(
		stuId INT,
		stuName VARCHAR(20),
		gender CHAR,
		bornDate DATETIME
	);
	
	二、查看表的详细信息
	 DESC studentinfo;
	
	三、修改表名
	 ALTER TABLE stuinfo RENAME [TO]  studentinfo;
	
	四、修改列
	 ALTER TABLE 表名 ADD|MODIFY|DROP|CHANGE COLUMN 字段名 【字段类型】;
	 ①修改字段名
	  ALTER TABLE studentinfo CHANGE  COLUMN sex gender CHAR;
	 ②修改字段类型和列级约束
	  ALTER TABLE studentinfo MODIFY COLUMN borndate DATE;
	 ③添加字段
	  ALTER TABLE studentinfo ADD COLUMN email VARCHAR(20) first;
	 ④删除字段
	  ALTER TABLE studentinfo DROP COLUMN email;
	  
	五、删除表
	  DROP TABLE [IF EXISTS] studentinfo;	
	
	六、表的复制
	 1.仅仅复制表的结构
	   CREATE TABLE copy LIKE author;
	 2.复制表的结构+数据
	   CREATE TABLE copy SELECT * FROM author;
	 3.只复制部分数据
	   CREATE TABLE copy SELECT id,au_name FROM author WHERE nation='中国';
	 4.仅仅复制某些字段
	   CREATE TABLE copy SELECT id,au_name FROM author WHERE 0;

## 2. 常见数据类型

### 整型

​               tinyint、smallint、mediumint、int/integer、bigint
字节数        1	          2		           3	               4		           8

特点：
① 如果不设置无符号还是有符号，默认是有符号，如果想设置无符号，需要添加unsigned关键字
② 如果插入的数值超出了整型的范围,会报out of range异常，并且插入临界值
③ 如果不设置长度，会有默认的长度
长度代表了显示的最大宽度，如果不够会用0在左边填充，但必须搭配zerofill使用！

### 小数
分类：
    1.浮点型
        float(M,D)
       double(M,D)
    2.定点型
       dec(M，D)
       decimal(M,D)

特点：

①M：整数部位+小数部位 D：小数部位
    如果超过范围，则插入临界值

②M和D都可以省略
   如果是decimal，则M默认为10，D默认为0
   如果是float和double，则会根据插入的数值的精度来决定精度

③定点型的精确度较高，如果要求插入数值的精度较高如货币运算等则考虑使用

原则：
所选择的类型越简单越好，能保存数值的类型越小越好

### 字符型
较短的文本：

 	char
	 varchar

其他：

binary和varbinary用于保存较短的二进制
enum用于保存枚举
set用于保存集合


较长的文本：
text
blob(较大的二进制)

特点：

	          写法		M的意思					     特点		空间的耗费	    效率
	char	char(M)		最大的字符数，可以省略，默认为1  固定长度的字符	比较耗费	    高
	
	varchar varchar(M)	最大的字符数，不可以省略		可变长度的字符	  比较节省	      低
### 日期型

分类：
	date只保存日期
	time 只保存时间
	year只保存年

​	datetime保存日期+时间
​	timestamp保存日期+时间


特点：

		       字节		范围		时区等的影响
	datetime    8	  1000——9999	 不受
	timestamp	4	  1970-2038	     受
	
	CREATE TABLE tab_date(
		t1 DATETIME,
		t2 TIMESTAMP
	);
	
	INSERT INTO tab_date VALUES(NOW(),NOW());
	SELECT * FROM tab_date;
	SHOW VARIABLES LIKE 'time_zone';
	SET time_zone='+9:00';
## 3. 常见约束

	    NOT NULL：非空，用于保证该字段的值不能为空
			比如姓名、学号等
		DEFAULT:默认，用于保证该字段有默认值
			比如性别
		PRIMARY KEY:主键，用于保证该字段的值具有唯一性，并且非空
			比如学号、员工编号等
		UNIQUE:唯一，用于保证该字段的值具有唯一性，可以为空
			比如座位号
		CHECK:检查约束【mysql中不支持】
			比如年龄、性别
		FOREIGN KEY:外键，用于限制两个表的关系，用于保证该字段的值必须来自于主表的关联列的值
			在从表添加外键约束，用于引用主表中某列的值
		    比如学生表的专业编号，员工表的部门编号，员工表的工种编号
		    
	主键和唯一的大对比：
			保证唯一性  是否允许为空    一个表中可以有多少个    是否允许组合
		主键	 √		   ×		     至多有1个           √，但不推荐
		唯一	 √		   √		     可以有多个          √，但不推荐
		
	外键：
		1、要求在从表设置外键关系
		2、从表的外键列的类型和主表的关联列的类型要求一致或兼容，名称无要求
		3、主表的关联列必须是一个key（一般是主键或唯一）
		4、插入数据时，先插入主表，再插入从表
		删除数据时，先删除从表，再删除主表
	
	1、添加列级约束
	alter table 表名 modify column 字段名 字段类型 新约束;
	
	2、添加表级约束
	alter table 表名 add 【constraint 约束名】 约束类型(字段名) 【外键的引用】

## 4. 数据库事务
### 含义
​	通过一组逻辑操作单元（一组DML——sql语句），将数据从一种状态切换到另外一种状态

   TCL  Transaction Control Language 事务控制语言

### 特点
​	（ACID）
​	**原子性**：要么都执行，要么都回滚
​	**一致性**：保证数据的状态操作前和操作后保持一致
​	**隔离性**：多个事务同时操作相同数据库的同一个数据时，一个事务的执行不受另外一个事务的干扰
​	**持久性**：一个事务一旦提交，则数据将持久化到本地，除非其他事务对其进行修改

### 事务的创建

	1.隐式事务：事务没有明显的开启和结束的标记
	  比如: insert、update、delete语句
	       delete from 表 where id =1;
	
	2.显式事务：事务具有明显的开启和结束的标记
	  前提：必须先设置自动提交功能为禁用(set autocommit=0;)
	
	3.创建显示事务
	  步骤1：开启事务
	        set autocommit=0;
	        start transaction;可选的
	  步骤2：编写事务中的sql语句(select insert update delete)
	        语句1;
	        语句2;
	        ...
	  步骤3：结束事务
	       commit;提交事务
	       rollback;回滚事务
	       
	  3.1 savepoint 的使用  
	  	     set autocommit=0;
	         start transaction;
	         savepoint  断点
	         commit to 断点
	         rollback to 断点

### 事务的隔离级别:

事务并发问题如何发生？

	当多个事务同时操作同一个数据库的相同数据时
事务的并发问题有哪些？

	脏读：一个事务读取到了另外一个事务未提交的数据
	不可重复读：同一个事务中，多次读取到的数据不一致
	幻读：一个事务读取数据时，另外一个事务进行更新，导致第一个事务读取到了没有更新的数据

事务的隔离级别

| 隔离级别\并发问题 | 脏读 | 不可重复读 | 幻读 |
| :---------------- | ---- | ---------- | ---- |
| read uncommitted  | √    | √          | √    |
| read committed    | ×    | √          | √    |
| repeatable read   | ×    | ×          | √    |
| serializable      | ×    | ×          | ×    |

设置隔离级别：

	set session|global  transaction isolation level 隔离级别名;
查看隔离级别：

	select @@tx_isolation;



## 5. 视图
含义：理解成一张虚拟的表

​    mysql5.1版本出现的新特性，是通过表动态生成的数据

视图和表的区别：
		 使用方式	  是否占用物理空间
	
	视图	完全相同	不占用，仅仅保存的是sql逻辑
	 
	表	 完全相同	 占用

视图的好处：


	1、sql语句提高重用性，效率高
	2、和表实现了分离，提高了安全性

视图的增删改查

	1、创建视图
		CREATE VIEW  视图名
		AS
		查询语句;
	
	2、查看视图的数据
	   SELECT * FROM my_v4;
	   SELECT * FROM my_v1 WHERE last_name='Partners';
	
	3、插入视图的数据
	   INSERT INTO my_v4(last_name,department_id) VALUES('虚竹',90);
	
	4、修改视图的数据
	   UPDATE my_v4 SET last_name ='梦姑' WHERE last_name='虚竹';
	   
	5、删除视图的数据
	   DELETE FROM my_v4; 
	
	6、视图结构的查看	
	   DESC test_v7;
	   SHOW CREATE VIEW test_v7;

 <Strong style="color:red">某些视图不能更新</strong>

```
   包含以下关键字的sql语句：分组函数、distinct、group  by、having、union或者union all
​	常量视图
​	Select中包含子查询
​	join
​	from一个不能更新的视图
​	where子句的子查询引用了from子句中的表
```


视图逻辑的更新

```
#方式一：
CREATE OR REPLACE VIEW test_v7
AS
SELECT last_name FROM employees
WHERE employee_id>100;
	
#方式二:
ALTER VIEW test_v7
AS
SELECT employee_id FROM employees;
```

## 6. 变量

系统变量：
	全局变量：作用域：针对于所有会话（连接）有效，但不能跨重启
	会话变量：作用域：针对于当前会话（连接）有效

自定义变量：
	用户变量
	局部变量

### 6.1 系统变量

说明：变量由系统定义，不是用户定义，属于服务器层面
注意：全局变量需要添加global关键字，会话变量需要添加session关键字，如果不写，默认会话级别

使用步骤：
 1、查看所有系统变量
	 show global|【session】variables;

 2、查看满足条件的部分系统变量
 	show global|【session】 variables like '%char%';

3、查看指定的系统变量的值
	 select @@global|【session】系统变量名;

4、为某个系统变量赋值
	方式一：
		set global|【session】系统变量名=值;
	方式二：
		set @@global|【session】系统变量名=值;

### 6.2 自定义变量

说明：变量由用户自定义，而不是系统提供的

**用户变量**

作用域：针对于当前会话（连接）有效，作用域同于会话变量

```
#赋值操作符：=或:=
#①声明并初始化
SET @变量名=值;
SET @变量名:=值;
SELECT @变量名:=值;

#②赋值（更新变量的值）
#方式一：
	SET @变量名=值;
	SET @变量名:=值;
	SELECT @变量名:=值;
#方式二：
	SELECT 字段 INTO @变量名
	FROM 表;
#③使用（查看变量的值）
SELECT @变量名;
```

**局部变量**

作用域：仅仅在定义它的begin end块中有效
          应用在 begin end中的第一句话

```
#①声明
DECLARE 变量名 类型;
DECLARE 变量名 类型 【DEFAULT 值】;


#②赋值（更新变量的值）

#方式一：
	SET 局部变量名=值;
	SET 局部变量名:=值;
	SELECT 局部变量名:=值;
#方式二：
	SELECT 字段 INTO 具备变量名
	FROM 表;
#③使用（查看变量的值）
SELECT 局部变量名;
```

**局部变量与用户变量的对比**

```
		  作用域			     定义位置		         语法
用户变量	当前会话		     会话的任何地方		加@符号，不用指定类型
局部变量	定义它的BEGIN END中 	BEGIN END的第一句话	一般不用加@,需要指定类型
```

## 7. 存储过程

存储过程和函数：类似于java中的方法
好处：
	1、提高代码的重用性
	2、简化操作

含义：一组经过预先编译的sql语句的集合

好处：

	1、提高了sql语句的重用性，减少了开发程序员的压力
	2、提高了效率
	3、减少了编译次数并且减少了和数据库服务器的连接次数，提高了效率

### 创建

	CREATE PROCEDURE 存储过程名(参数列表)
	BEGIN
	存储过程体（一组合法的SQL语句）
	END
**注意**：

```
1、参数列表包含三部分
	参数模式  参数名  参数类型
	举例： in stuname varchar(20)

   参数模式：
		in：该参数可以作为输入，也就是该参数需要调用方传入值
		out：该参数可以作为输出，也就是该参数可以作为返回值
		inout：该参数既可以作为输入又可以作为输出，也就是该参数既需要传入值，又可以返回值

2、如果存储过程体仅仅只有一句话，begin end可以省略
   存储过程体中的每条sql语句的结尾要求必须加分号。
   存储过程的结尾可以使用 delimiter 重新设置
 语法：
  delimiter 结束标记
  案例：
  delimiter $
```

### 调用语法

  见【doc/存储过程.sql】

### 删除存储过程
#语法：drop procedure 存储过程名
DROP PROCEDURE p1;
DROP PROCEDURE p2,p3;#×

### 查看存储过程的信息
DESC myp2;×
SHOW CREATE PROCEDURE  myp2;

## 8. 函数

**创建函数**

	学过的函数：LENGTH、SUBSTR、CONCAT等
	CREATE FUNCTION 函数名(参数名 参数类型,...) RETURNS 返回类型
	BEGIN
		函数体
	END
	
	注意：
	1.参数列表 包含两部分：
	   参数名 参数类型
	
	2.函数体：肯定会有return语句，如果没有会报错
	  如果return语句没有放在函数体的最后也不报错，但不建议
	  return 值;
	  
	3.函数体中仅有一句话，则可以省略begin end
	
	4.使用 delimiter语句设置结束标记

**调用函数**
	SELECT 函数名（实参列表）

**函数和存储过程的区别**

			  关键字		 调用语法	       返回值			应用场景
	函数		 FUNCTION	 SELECT 函数()    只能是一个		一般用于查询结果为一个值并返回时，当有返回值而且仅仅一个
	存储过程	PROCEDURE	CALL 存储过程()	 可以有0个或多个	一般用于更新

## 9. 流程控制结构

顺序、分支、循环

### 分支
1、if函数
	语法：if(条件，值1，值2)
	特点：可以用在任何位置

2、case语句

语法：

	情况一：类似于switch
	case 表达式
	when 值1 then 结果1或语句1(如果是语句，需要加分号) 
	when 值2 then 结果2或语句2(如果是语句，需要加分号)
	...
	else 结果n或语句n(如果是语句，需要加分号)
	end 【case】（如果是放在begin end中需要加上case，如果放在select后面不需要）
	
	情况二：类似于多重if
	case 
	when 条件1 then 结果1或语句1(如果是语句，需要加分号) 
	when 条件2 then 结果2或语句2(如果是语句，需要加分号)
	...
	else 结果n或语句n(如果是语句，需要加分号)
	end 【case】（如果是放在begin end中需要加上case，如果放在select后面不需要）


特点：
	可以用在任何位置

3、if elseif语句

语法：

	if 情况1 then 语句1;
	elseif 情况2 then 语句2;
	...
	else 语句n;
	end if;

特点：
	**只能用在begin end中！！！！！！！！！！！！！！！**

三者比较：
			          应用场合
	if函数		 简单双分支
	case结构	等值判断 的多分支
	if结构		 区间判断 的多分支

### 循环

分类：
    while、loop、repeat

循环控制：
	iterate类似于 continue，继续，结束本次循环，继续下一次
	leave 类似于  break，跳出，结束当前所在的循环

语法：


	1.while
	【标签:】while 循环条件 do
		循环体;
	end while【 标签】;
	
	2.loop
	【标签:】loop
		循环体;
	end loop 【标签】;
	可以用来模拟简单的死循环
	
	3.repeat
	【标签：】repeat
		循环体;
	until 结束循环的条件
	end repeat 【标签】;

特点：

	只能放在BEGIN END里面
	如果要搭配leave跳转语句，需要使用标签，否则可以不用标签
	leave类似于java中的break语句，跳出所在循环！！！



































