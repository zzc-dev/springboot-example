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