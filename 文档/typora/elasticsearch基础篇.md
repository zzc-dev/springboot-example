# 一、es概念

| **RDBS**            | **ES**                                         |
| ------------------- | ---------------------------------------------- |
| 数据库（database）  | 索引（index）                                  |
| 表（table）         | 类型（type）（ES6.0之后被废弃，es7中完全删除） |
| 表结构（schema）    | 映射（mapping）                                |
| 行（row）           | 文档（document）                               |
| 列（column）        | 字段（field）                                  |
| 索引                | 反向索引                                       |
| SQL                 | 查询DSL                                        |
| SELECT * FROM table | GET http://.....                               |
| UPDATE table SET    | PUT http://......                              |
| DELETE              | DELETE http://......                           |

## 1. Elasticsearch概述

ES是基于Lucene的搜索服务器，它提供了一个分布式多用户能力的全问搜索引擎，且ES支持RestFulweb风格的url访问。ES是基于Java开发的开源搜索引擎，设计用于云计算，能够达到实时搜索，稳定、可靠、快速。此外，ES还提供了数据聚合分析功能，但在数据分析方面，es的时效性不是很理想，在企业应用中一般还是用于搜索。ES自2016年起已经超过Solr等，称为排名第一的搜索引擎应用。

## 2. 类型废弃原因

我们一直认为ES中的“index”类似于关系型数据库的“database”，而“type”相当于一个数据表。ES的开发者们认为这是一个糟糕的认识。例如：关系型数据库中两个数据表示是独立的，即使他们里面有相同名称的列也不影响使用，但ES中不是这样的。

我们都知道elasticsearch是基于Lucene开发的搜索引擎，而ES中不同type下名称相同的filed最终在Lucene中的处理方式是一样的。举个例子，两个不同type下的两个user_name，在ES同一个索引下其实被认为是同一个filed，你必须在两个不同的type中定义相同的filed映射。否则，不同type中的相同字段名称就会在处理中出现冲突的情况，导致Lucene处理效率下降。

去掉type能够使数据存储在独立的index中，这样即使有相同的字段名称也不会出现冲突，就像ElasticSearch出现的第一句话一样“你知道的，为了搜索····”，去掉type就是为了提高ES处理数据的效率。

除此之外，在同一个索引的不同type下存储字段数不一样的实体会导致存储中出现稀疏数据，影响Lucene压缩文档的能力，导致ES查询效率的降低

## 3. 分片(Shard)和副本（Replica）

![img](D:\myself\springboot-example\文档\typora\images\es01.png)

**分片**

如果我们的索引数据量很大，超过`硬件存放单个文件`的限制，就会影响查询请求的速度，
Es引入了分片技术。一个分片本身就是一个完成的搜索引擎，文档存储在分片中，而分片会被分配到集群中的各个节点中，随着集群的扩大和缩小，ES会自动的将分片在节点之间进行迁移，以保证集群能保持一种平衡。分片有以下特点：

1. ES的一个索引可以包含多个分片（shard）；
2. 每一个分片（shard）都是一个最小的工作单元，承载部分数据；
3. **每个shard都是一个lucene实例**，有完整的简历索引和处理请求的能力；
4. 增减节点时，shard会自动在nodes中负载均衡；
5. 一个文档只能完整的存放在一个shard上
6. 一个索引中含有shard的数量，默认值为5，在索引创建后这个值是不能被更改的。
7. 优点：水平分割和扩展我们存放的内容索引；分发和并行跨碎片操作提高性能/吞吐量；
8. 每一个shard关联的副本分片（replica shard）的数量，默认值为1，这个设置在任何时候都可以修改。

**副本**

副本（replica shard）就是shard的冗余备份，它的主要作用：

1. 冗余备份，防止数据丢失；
2. shard异常时负责容错和负载均衡；

主分片与副本都能处理查询请求, 它们的唯一区别在于只有主分片才能处理索引请求.

## 4. 聚合

https://blog.csdn.net/qq_40728028/article/details/105246120

# *analyzer和search_analyzer的区别

```
{
    "settings": {
        "index": {
            "number_of_replicas": "0",
            "number_of_shards": "5",
            "refresh_interval": "-1",
            "translog.flush_threshold_ops": "100000"
        }
    },
    "mappings": {
        "etp_t": {
            "properties": {
                "dd": {
                    "type": "multi_field",
                    "fields": {
                        "pn": {
                            "type": "string",
                            "store": "yes",
                            "analyzer": "pinyin_first_letter",
                            "search_analyzer": "pinyin_first_letter"
                        }
                    }
                }
            }
        }
    }
}
```

分析器主要有两种情况会被使用：
第一种是插入文档时，将text类型的字段做分词然后插入倒排索引，
第二种就是在查询时，先对要查询的text类型的输入做分词，再去倒排索引搜索

如果想要让 索引 和 查询 时使用不同的分词器，ElasticSearch也是能支持的，只需要在字段上加上search_analyzer参数

​       在索引时，只会去看字段有没有定义analyzer，有定义的话就用定义的，没定义就用ES预设的

​        在查询时，会先去看字段有没有定义search_analyzer，如果没有定义，就去看有没有analyzer，再没有定义，才会去使用ES预设的

# 索引流程图示

![img](D:\myself\springboot-example\文档\typora\images\es05.png)

![image-20201103194129165](D:\myself\springboot-example\文档\typora\images\es06.png)

# 倒排索引

>倒排索引源于实际应用中需要根据属性的值来查找记录。这种索引表中的每一项都包括一个属性值和具有该属性值的各记录的地址。由于不是由记录来确定属性值，而是由属性值来确定记录的位置，因而称为倒排索引(inverted index)。带有倒排索引的文件我们称为倒排[索引文件](https://baike.baidu.com/item/索引文件)，简称[倒排文件](https://baike.baidu.com/item/倒排文件/4137688)(inverted file)。
>
>1.正排索引： 由文档指向关键词
>
> 文档--> 单词1 ,单词2
>
> 
>
>2.倒排索引： 由关键词指向文档
>
>单词1--->文档1,文档2，文档3
>
>单词2--->文档1，文档2

## 1. 数据举例

![在这里插入图片描述](D:\myself\springboot-example\文档\typora\images\es02.png)

![image-20201102203105726](D:\myself\springboot-example\文档\typora\images\es03.png)

Elasticsearch分别为每个field分词后都建立了一些倒排索引（**以下举例默认不分词**），24，Kate, John Female这些叫term，而[1,2]就是Posting List倒排列表。Posting list就是一个int的数组，倒排列表记录了出现过某个单词的所有文档的文档列表及单词在该文档中出现的位置信息，每条记录称为一个倒排项(Posting)。根据倒排列表，即可获知哪些文档包含某个单词。

> 思考：如果这里有上千万的记录呢？如何通过term来查找呢？这就需要了解一下Term Dictionary和Term Index的概念

## 2. Term index

这棵树不会包含所有的term，它包含的是term的一些前缀。通过term index可以快速地定位到term dictionary的某个offset，然后从这个位置再往后顺序查找

## 3. Term Dictionary

Elasticsearch为了能快速找到某个term，将所有的term排个序，二分法查找term，logN的查找效率，就像通过字典查找一样，这就是Term Dictionary

## 4. 总结

![倒排列表](D:\myself\springboot-example\文档\typora\images\es04.png)

term index不需要存下所有的term，而仅仅是他们的一些前缀与Term Dictionary的block之间的映射关系，再结合FST(Finite State Transducers)的压缩技术，可以使term index缓存到内存中。从term index查到对应的term dictionary的block位置之后，再去磁盘上找term，大大减少了磁盘随机读的次数。