> curl -X<VERB> '<PROTOCOL>://<HOST>:<PORT>/<PATH>?<QUERY_STRING>' -d '<BODY>'

**`<VERB>`**
	适当的HTTP方法或动词。例如，`GET`，`POST`， `PUT`，`HEAD`，或`DELETE`。



```json
# 查看安装的插件
GET /_cat/plugins?v
# es集群节点
GET /_cat/nodes
# es集群健康情况
GET /_cluster/health
```

索引状态

```json
# 查看索引相关信息
GET kibana_sample_data_ecommerce

# 查看索引的文档总数
GET kibana_sample_data_ecommerce/_count

# 查看前10条文档，了解文档格式
POST kibana_sample_data_ecommerce/_search
{
}

#_cat indices API
# 查看indices
GET /_cat/indices/kibana*?v&s=index

# 查看状态为绿的索引
GET /_cat/indices?v&health=green
 
# 按照文档个数排序
GET /_cat/indices?v&s=docs.count:desc

# 查看具体的字段
GET /_cat/indices/kibana*?pri&v&h=health,index,pri,rep,docs.count,mt

#How much memory is used per index?
GET /_cat/indices?v&h=i,tm&s=tm:desc
```



更新数据需要指定doc

```json
put /mapping_test/_doc/1
{
  "firstName":"Chan",
  "lastName": "Jackie",
  "loginDate":"2018-07-24T10:29:48.103Z"
}

#更新字段
post /mapping_test/_doc/1/_update
{
  "doc":{"isAdmin":"tet2"}
}
```

dynamic mapping

```java
// 默认映射数值、日期、布尔，所有字符串都将映射为text和keyword
// "interests":["reading","music"]：数组也是字符串类型

// 设置为false后，新增的字段无法作为条件搜素，结果集有
// 设置为strict后，无法新增字段
put /mapping_test/_mapping
{
  "dynamic": false  
}

// 设置mapping
put /mapping_test
{
    "mapping":{
        "properties":{
            "fieldName":{
                "type": "text",
                "index": false, // 该字段不会建立索引
                "null_value":"NULL" // 不能为null
            }
        }
    }
}
```







# 一、es增删改

## 1. 添加index及mapping

**格式：**

```
PUT /index_name/_mapping
{
	"properties":{
		"prop_name":{
			"type": "具体类型",
			"index": true,
			"store": false,
			"enabled": true,
			"fields":{  fields设置keyword，上面的type设置text，该字段既可以精确搜索，又可以全文匹配
				"keyword" : {
                	"type" : "keyword",
                	"ignore_above" : 256
            	}
			}
		},
		"prop_name1":{
			"properties":{
				prop_name1相当于map，这里写的格式和prop_name1一致，相当于prop_name1的一个键
			}
		}
	}
}
```

**ignore_above**

对超过 `ignore_above` 的字符串，analyzer 不会进行处理；所以就不会索引起来。导致的结果就是最终搜索引擎搜索不到了。这个选项主要对 `not_analyzed` 字段有用，这些字段通常用来进行过滤、聚合和排序。而且这些字段都是结构化的，所以一般不会允许在这些字段中索引过长的项。

**举例：**

```
PUT /hyomin/
PUT /hyomin/_mapping
{
  "properties" : {
    "attr" : {
      "properties" : {
        "brand" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        }
    },
    "images" : {
      "type" : "keyword",
      "index" : false
    },
    "price" : {
      "type" : "long"
    },
    "title" : {
      "type" : "text",
      "analyzer" : "ik_max_word"
    }
  }
}
```

## 2. 添加文档

如果该请求`customer`尚不存在，该请求将自动创建该索引，并存储字段并为其建立索引。

```json
# 自定义id  如果该id存在，会更新整个文档
PUT /{index}/{type}/{id}
{
  "field": "value",
  ...
}

# 自动生成id
POST /{index}/{type}/
{
  "field": "value",
  ...
}
    
PUT /website/blog/123?op_type=create
{ ... }

PUT /website/blog/123/_create
{ ... }
```

## 3. 更新文档

```json
# 更新部分
POST /website/blog/1/_update
{
   "doc" : {
      "tags" : [ "testing" ],
      "views": 0
   }
}

# 自定义id  如果该id存在，会更新整个文档
PUT /{index}/{type}/{id}
{
  "field": "value",
  ...
}
```

**更新文档尚未存在**

```json
# upsert: 不存在则先创建
POST /website/pageviews/1/_update
{
   "script" : "ctx._source.views+=1",
   "upsert": {
       "views": 1
   }
}
```

## 4. **批量索引文件**

如果您有很多要编制索引的文档，则可以使用[批量API](https://www.elastic.co/guide/en/elasticsearch/reference/7.6/docs-bulk.html)批量提交。使用批量处理批处理文档操作比单独提交请求要快得多，因为它可以最大程度地减少网络往返次数。

```
curl -H "Content-Type: application/json" -XPOST "localhost:9200/bank/_bulk?pretty&refresh" --data-binary "@D:\myself\accounts.json"
```

```json
# 除了delete没有请求体
# 每个子请求都是独立执行，因此某个子请求的失败不会对其他子请求的成功与否造成影响。 如果其中任何子请求失败，最顶层的 error 标志被设置为 true ，并且在相应的请求报告出错误明细
POST /_bulk
{ "delete": { "_index": "website", "_type": "blog", "_id": "123" }}   # 1.要有换行符
{ "create": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "title":    "My first blog post" }
{ "index":  { "_index": "website", "_type": "blog" }}
{ "title":    "My second blog post" }
{ "update": { "_index": "website", "_type": "blog", "_id": "123", "_retry_on_conflict" : 3} }
{ "doc" : {"title" : "My updated blog post"} } # 2.最后一行也要有换行符
```

## 5. 关于es映射mapping中的enabled，store，index参数的理解

**enabled**

> 默认是true。只用于mapping中的object字段类型。当设置为false时，**其作用是使es不去解析该字段，并且该字段不能被查询和store**，只有在_source中才能看到（即查询结果中会显示的_source数据）。设置enabled为false，可以不设置字段类型，默认为object

**store**

>默认false。store参数的功能和_source有一些相似。我们的数据默认都会在_source中存在。但我们也可以将数据store起来，不过大部分时候这个功能都很鸡肋。不过有一个例外，当我们使用copy_to参数时，copy_to的目标字段并不会在_source中存储，此时store就派上用场了。

**三者能否同时存在：**
  首先设置了enabled为false就不能设置store为true了，这两者冲突。而index和store是不冲突的。最后index和enabled之间的问题：enabled需要字段类型为object，而当字段类型为object时，好像不能设置index参数，试了几次都会报错。

## 6. 删除数据

```sense
DELETE /website/blog/123
```

```
POST https://dolphin-dev.kedacom.com/es-common/haiyan_vehicle_file_zzc/a_hy_vehicle/_delete_by_query

{
    "query":{
        "wildcard":{
            "HPHM": "苏E1*"
        }
    }
}
```



# 二、查询结果字段解释

   **默认情况下，`hits`响应部分包括符合搜索条件的前10个文档**：

- `took` – Elasticsearch运行查询多长时间（以毫秒为单位）

- `timed_out` –搜索请求是否超时

  应当注意的是 `timeout` 不是停止执行查询，它仅仅是告知正在协调的节点返回到目前为止收集的结果并且关闭连接。在后台，其他的分片可能仍在执行查询即使是结果已经被发送了

- `_shards` –搜索了多少个分片，以及成功，失败或跳过了多少个分片。

- `max_score` –找到的最相关文件的分数

- `hits.total.value` -找到了多少个匹配的文档

- `hits.sort` -文档的排序位置（不按相关性得分排序时）

- `hits._score`-文档的相关性得分（使用时不适用`match_all`）

# 三、settings

## 3.1 设置分片数和副本数

```json
PUT /blogs
{
   "settings" : {
      "number_of_shards" : 3,
      "number_of_replicas" : 1
   }
}
```

## 3.2 开启或者关闭自动创建索引

```
PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "false" 
    }
}
```

## 3.3 限制深分页

```json
{
	"settings":{
		"index":{
			"max_result_window": 10000 // form+size<=10000
		}
	}
}
```

# 四、mapping

## 4.1 创建

`_all`元字段也将在7.0版本中移除, 它建议我们使用`copy_to`定制自己的`all field`

每个属性的键：

- `type`：field的类型
- `analyzer`：分词器
- `index`：该字段能否被索引，是否需要建立倒排索引
- `format`：日期格式化

```
PUT website
{
    "mappings": {
        "user": {       // 这就是一个root object
            "_all": { "enabled": false },  // 禁用_all字段
            "properties": {
                "user_id": { "type": "text" },
            	  "name": {
                    "type": "text",
                    "analyzer": "english"
                },
                "age": { "type": "integer" },
                "sex": { "type": "keyword" },
                "birthday": {
                    "type": "date", 
                    "format": "strict_date_optional_time||epoch_millis"
                },
                "address": {
                    "type": "text",
                    "index": false         // 不分词
                },
                "phone":{
                	 "type": "text",
                	 "fields": {
                     "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                     }
                } 
            }
        }
    }
}
```

可以对同一个字段建立两个索引

```
"title":{
	"type":"text",
	"analyzed":"standard",
	"fields":{
		"title.english":{
			"type": "text",
			"analyzed": "english"
		}
	}
}
```



## 4.2 更新

无法更新原有field的mapping，但可以新增字段

```
PUT website/_mapping		// 修改website索引的_mapping, 注意API的顺序
{
    "properties": {
        "new_field": {
            "type": "text",
            "index": false
        }
    }
}
```

## 4.3 查看

```
get website/_mapping
```



# 五、一般查询

## 5.1 参数

- pretty：调用 Elasticsearch 的 *pretty-print* 功能，该功能 使得 JSON 响应体更加可读
- timeout

## 5.2 查询所有文档

```
# 查询所有索引的全部文档
get /_search
```

```
# 全文排序查询 默认只查10条
GET /bank/_search?pretty
{
  "query": { "match_all": {} },
  "sort": [
    { "account_number": "asc" }
  ]
}
```

## 5.3 多索引查询

```
/gb/_search
/gb,us/_search
/g*,u*/_search 在任何以 g 或者 u 开头的索引中搜索所有的类型
```



```
# 检查文档是否存在
curl -i -XHEAD http://localhost:9200/website/blog/123
```



```sense
# 返回特定字段
GET /website/blog/123?_source=title,text
```

```
# 从from开始，查询size条数据
GET /bank/_search?pretty
{
  "query": { "match_all": {} },
  "sort": [
    { "account_number": "asc" }
  ],
  "from": 10,
  "size": 10

}
```

**match** 只要文档中包含分词后的字段就匹配
    该`address`字段以查找地址包含`mill`或的客户`lane`

```
GET /bank/_search?pretty
{
  "query": { "match": { "address": "mill lane" } }
}
```

**match_phrase** 要执行词组搜索而不是匹配单个词，请使用 `match_phrase`代替`match`
    以下请求仅匹配**包含**短语的地址`mill lane`：

```
GET /bank/_search?pretty
{
  "query": { "match_phrase": { "address": "mill lane" } }
}
```

# 五、复杂查询

## 5.1 多索引批量查询

`_mget`

```
get /_mget
{
  "docs":[
    {
      "_index":"user1",
      "_type":"_doc",
      "_id": 1
    },{
      "_index":"user2",
      "_type":"_doc",
      "_id": 1
    }
  ]
}

# 响应
{
  "docs" : [
    {
      "_index" : "user1",
      "_type" : "_doc",
      "_id" : "1",
      "_version" : 1,
      "_seq_no" : 0,
      "_primary_term" : 1,
      "found" : true,
      "_source" : {
        "name" : "zzc"
      }
    }
  ]
}
```

## 5.2 判断值是否为空

```json
#相当于select * from users where title is not null
get /users/_search
{
  "query":{
    "bool":{
      "must":{  # must_not 相反：查找title=null的文档
        "exists":{
          "field": "title"
        }
      }
      
    }
  }
}
```

# 六、简易查询 query-string search

```json
# 查询myindex索引中name包含zzc的文档
GET /myindex/_doc/_search?q=name:zzc 
```

查询字符串参数需要URL编码

```json
# + 前缀表示必须与查询条件匹配 
# - 前缀表示一定不与查询条件匹配。
# 没有 + 或者 - 的所有其他条件都是可选的——匹配的越多，文档就越相关。
GET /_search?q=%2Bname%3Ajohn%2Btweet%3Amary ==>  +name:john +tweet:mary
```

# 七、验证查询

```json
# 验证查询语法是否正确  加上参数 ?explain 可以得到失败或者成功的详细信息
get /user1/_validate/query
{
  "query":{
    "match1":{
      "age": "zzc"
    }
  }
}
```

# 八、结构化搜索和全文检索

**结构化搜索**

​	对于结构化文本来说，一个值要么相等，要么不等。没有 *更似* 这种概念。

​	结构化查询不关心文件的相关度或评分；它简单的对文档包括或排除处理。

​    `term`  `terms`

**全文搜索**

​	**相关性（Relevance）** + **分析（Analysis）**

# es查询（各字段解释）

es中的查询请求有两种方式，一种是简易版的查询，另外一种是使用JSON完整的请求体，叫做结构化查询（DSL）。
由于DSL查询更为直观也更为简易，所以大都使用这种方式。
DSL查询是POST过去一个json，由于post的请求是json格式的，所以存在很多灵活性，也有很多形式。
这里有一个地方注意的是官方文档里面给的例子的json结构只是一部分，并不是可以直接黏贴复制进去使用的。一般要在外面加个query为key的机构。

## 1. match

> **匹配字段会进行分词，只要包含分词字段就匹配**

```json
{
  "query": {
    "match": {
        "title":  "cat dog"
    }
  }
}

# query： 查询文本
# operator： 提高精度and or
# minimum_should_match 控制精度 
#          数字n：一个文档必须匹配n个分词 
#          n%：一个文档必须匹配的分词/query分词后长度 >= n%
# boost：n 在bool查询中n值越大，该查询语句的权重就越高
{
    "query": {
        "match": {
            "title": {      
                "query":    "BROWN DOG!",
                "operator": "and"
            }
        }
    }
}
```

## 2. match_phrase

> **匹配字段会进行分词，必须包含所有分词字段的文档才会匹配**
>
>    注意：是包含，并不是等于。比如：搜索“no zuo”，“no zuo no die”也会匹配

比如上面一个例子，一个文档"我的保时捷马力不错"也会被搜索出来，那么想要精确匹配所有同时包含"宝马 多少 马力"的文档怎么做？就要使用 match_phrase 了

```
{
  "query": {
    "match_phrase": {
        "content" : {
            "query" : "我的宝马多少马力"
        }
    }
  }
}
```

完全匹配可能比较严，我们会希望有个可调节因子，少匹配一个也满足，那就需要使用到slop。

```
https://www.elastic.co/guide/cn/elasticsearch/guide/current/slop.html
{
  "query": {
    "match_phrase": {
        "content" : {
            "query" : "我的宝马多少马力",
            "slop" : 1
        }
    }
  }
}
```

### 2.1 多值字段的匹配

```json
PUT /my_index/groups/1
{
    "names": [ "John Abraham", "Lincoln Smith"]
}

# 对 Abraham Lincoln 进行match_phrase查询，也能匹配，不合乎常识
原因：
	在分析 John Abraham 的时候， 产生了如下信息：
        Position 1: john
        Position 2: abraham
	在分析 Lincoln Smith 的时候， 产生了：
        Position 3: lincoln
        Position 4: smith
     以上数组分析生成了与分析单个字符串 John Abraham Lincoln Smith 
#解决方案：position_increment_gap，这样匹配该文档就需要slop=100
PUT /my_index/_mapping/groups 
{
    "properties": {
        "names": {
            "type":                "text",
            "position_increment_gap": 100
        }
    }
}
	在分析 John Abraham 的时候， 产生了如下信息：
        Position 1: john
        Position 2: abraham
	在分析 Lincoln Smith 的时候， 产生了：
        Position 103: lincoln
        Position 104: smith
```



## 3. multi_match

> 如果我们希望两个字段进行匹配，其中一个字段有这个文档就满足的话，使用multi_match

```json
# query 匹配文档
     # ["title","body^2"] 提升字段的权重， body的boost为2
# fields 匹配的字段
# type 匹配类型
# tie_breaker 没有完全匹配文档的评分系数，默认为0，不参与评分
# minimum_should_match 
{
  "query": {
    "multi_match": {
        "query" : "我的宝马多少马力",
        "fields" : ["title", "content"]
    }
  }
}
```

但是multi_match就涉及到匹配评分的问题了。

### 3.1 best_fields

> 完全匹配的文档占的评分比较高
>
> 字段中心式（field-centric）

```json
# 完全匹配"宝马 发动机"的文档评分会比较靠前，如果只匹配宝马的文档评分乘以0.3的系数
{
  "query": {
    "multi_match": {
      "query": "宝马 发动机",
      "type": "best_fields",
      "fields": [
        "tag",
        "content"
      ],
      "tie_breaker": 0.3
    }
  }
}

## dis_max
# 1个简单的 dis_max 查询会采用单个最佳匹配字段，而忽略其他的匹配：
		# 文档1 tag：宝马 content：xx 文档2 tag：宝马 content：发动机， 两者评分一致

# `tie_breaker` 这个参数将其他匹配语句的评分也考虑其中
{
    "query": {
        "dis_max": {
            "queries": [
                { "match": { "tag": "宝马 发动机" }},
                { "match": { "content":  "宝马 发动机" }}
            ]
            # ,"tie_breaker": 0.3
        }
    }
}
```

### 3.2 most_fields

> 越多字段匹配的文档评分越高
>
> 字段中心式（field-centric）

```
{
  "query": {
    "multi_match": {
      "query": "我的宝马发动机多少",
      "type": "most_fields",
      "fields": [
        "tag",
        "content"
      ]
    }
  }
}
```

### 3.3 cross_fields

> 词条的分词词汇是分配到不同字段评分越高
>
> 词中心式（term-centric）

```
{
  "query": {
    "multi_match": {
      "query": "我的宝马发动机多少",
      "type": "cross_fields",
      "fields": [
        "tag",
        "content"
      ]
    }
  }
}
```

## 4. term

> term是代表完全匹配，即不进行分词器分析，文档中必须<strong style='color:red'>包含</strong>整个搜索的词汇
>
> 与match_phrase不同，一个是包含分词后的所有字段，一个是包含不分词的整个字段
>    相同点是 文档中包含该字段就可以，并不是完全相等

```
{
  "query": {
    "term": {
      "content": "汽车保养"
    }
  }
}
```

查出的所有文档都包含"汽车保养"这个词组的词汇。

使用term要确定的是这个字段是否“被分析”(analyzed)，默认的字符串是被分析的。

拿官网上的例子举例：

mapping是这样的：

```
PUT my_index
{
  "mappings": {
    "my_type": {
      "properties": {
        "full_text": {
          "type":  "string"
        },
        "exact_value": {
          "type":  "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}

PUT my_index/my_type/1
{
  "full_text":   "Quick Foxes!",
  "exact_value": "Quick Foxes!"  
}
```

其中的full_text是被分析过的，所以full_text的索引中存的就是[quick, foxes]，而extra_value中存的是[Quick Foxes!]。

那下面的几个请求：

```
GET my_index/my_type/_search
{
  "query": {
    "term": {
      "exact_value": "Quick Foxes!"
    }
  }
}
```

请求的出数据，因为完全匹配

```
GET my_index/my_type/_search
{
  "query": {
    "term": {
      "full_text": "Quick Foxes!"
    }
  }
}
```

请求不出数据的，因为full_text分词后的结果中没有[Quick Foxes!]这个分词。

## 5. terms

`terms` 查询和 `term` 查询一样，但它允许你指定多值进行匹配。如果这个字段包含了指定值中的任何一个值，那么这个文档满足条件：

```sense
{ "terms": { "tag": [ "search", "full_text", "nosql" ] }}
```

和 `term` 查询一样，`terms` 查询对于输入的文本不分析。它查询那些精确匹配的值（包括在大小写、重音、空格等方面的差异）

## 6. range

`gt`   >           `lt` <     `gte` >=        `lte` <=

```sense
{
    "range": {
        "age": {
            "gte":  20,
            "lt":   30
        }
    }
}
```

## 7. bool联合查询

- `must`: 文档 *必须* 匹配这些条件才能被包含进来

- `must_not`: 文档 *必须不* 匹配这些条件才能被包含进来

- `should`:  如果满足这些语句中的任意语句，将增加 `_score` ，否则，无任何影响。它们主要用于修正每个文档的相关性得分。

- `filter`*必须* 匹配，但它以不评分、过滤模式来进行。这些语句对评分没有贡献，只是根据过滤标准来排除或包含文档。

  ​	结果会被缓存到内存中以便快速读取

### 7.1 filter

#### 7.1.1 filter缓存 

 1. ***查找匹配文档***.

 2. ***创建 bitset***.

    过滤器会创建一个 *bitset* （一个包含 0 和 1 的数组），它描述了哪个文档会包含该 term 。

 3. ***迭代 bitset(s)***

    一旦为每个查询生成了 bitsets ，Elasticsearch 就会循环迭代 bitsets 从而找到满足所有过滤条件的匹配文档的集合。

 4. ***增量使用计数***.

    Elasticsearch 会为每个索引跟踪保留查询使用的历史状态。如果查询在最近的 256 次查询中会被用到，那么它就会被缓存到内存中。当 bitset 被缓存后，缓存会在那些低于 10,000 个文档（或少于 3% 的总索引数）的段（segment）中被忽略。这些小的段即将会消失，所以为它们分配缓存是一种浪费。

**自动缓存行为**

​	如果一个非评分查询在最近的 256 次查询中被使用过（次数取决于查询类型），那么这个查询就会作为缓存的候选。

​	一旦缓存了，非评分计算的 bitset 会一直驻留在缓存中直到它被剔除。剔除规则是基于 LRU 的：一旦缓存满了，最近最少使用的过滤器会被剔除。

#### 7.1.2 组合filter

```sense
GET /my_store/_search
{
   "query" : {
      "bool" : { 
         "filter" : {
            "bool" : {
              "should" : [
                 { "term" : {"price" : 20}}, 
                 { "term" : {"productID" : "XHDK-A-1293-#fJ3"}} 
              ],
              "must_not" : {
                 "term" : {"price" : 30} 
              }
           }
         }
      }
   }
}
```

### 7.2 should

```json
# minimum_should_match 有多少个should应该匹配：数字或者百分比
{
  "query": {
    "bool": {
      "should": [
        { "match": { "title": "brown" }},
        { "match": { "title": "fox"   }},
        { "match": { "title": "dog"   }}
      ],
      "minimum_should_match": 2 
    }
  }
}
```

 

## 8. constant_score

​	相当于bool中只有一个filter

```sense
{
    "constant_score":   {
        "filter": {
            "term": { "category": "ebooks" } 
        }
    }
}
```

## 9. copy to

```sense
PUT /my_index
{
    "mappings": {
        "person": {
            "properties": {
                "first_name": {
                    "type":     "string",
                    "copy_to":  "full_name" 
                },
                "last_name": {
                    "type":     "string",
                    "copy_to":  "full_name" 
                },
                "full_name": {
                    "type":     "string"
                }
            }
        }
    }
}
```

## 10. shingles

https://www.elastic.co/guide/cn/elasticsearch/guide/current/shingles.html

## 11. prefix 部分查询

```json
{
    "query": {
        "prefix": {
            "postcode": "W1"
        }
    }
}
# 搜索前不分析如何在倒排索引中查询？
	1. 扫描词列表并查找到第一个以 W1 开始的词。
    2. 搜集关联的文档 ID 。
    3. 移动到下一个词。
    4. 如果这个词也是以 W1 开头，查询跳回到第二步再重复执行，直到下一个词不以 W1 为止。

# prefix 查询或过滤对于一些特定的匹配是有效的，但使用方式还是应当注意。当字段中词的集合很小时，可以放心使用，但是它的伸缩性并不好，会对我们的集群带来很多压力。可以使用较长的前缀来限制这种影响，减少需要访问的量。
```

`prefix` 查询是一个词级别的底层的查询

>默认状态下， `prefix` 查询不做相关度评分计算，它只是将所有匹配的文档返回，并为每条结果赋予评分值 `1` 。
>它的行为更像是过滤器而不是查询。 
>`prefix` 查询和 `prefix` 过滤器这两者实际的区别就是过滤器是可以被缓存的，而查询不行。

## 12. wildcard

 `wildcard` 通配符查询也是一种底层基于词的查询

 `?` 匹配任意字符， `*` 匹配 0 或多个字符。

支持正则表达式

## 13. match_phrase_prefix

这种查询的行为与 `match_phrase` 查询一致，不同的是它将查询字符串的最后一个词作为前缀使用，

```json
# max_expansions 可选
	# 控制着可以与前缀匹配的词的数量，它会先查找第一个与前缀 bl 匹配的词，然后依次查找搜集与之匹配的词（按字母顺序），直到没有更多可匹配的词或当数量超过 max_expansions 时结束。
{
    "match_phrase_prefix" : {
        "brand" : {
            "query": "walker johnnie bl", 
            "slop":  10,
            "max_expansions": 50 
        }
    }
}
```

# es聚合

*桶（Buckets）*

​	满足特定条件的文档的集合

*指标（Metrics）*

​	对桶内的文档进行统计计算

```json
# 1. text类型是无法聚合统计的，没有优化的字段es默认是禁止聚合/排序操作的。所以需要将要聚合的字段添加优化
{
  "properties": {
    "col54": { 
      "type":     "text",
      "fielddata": true # 使text字段添加优化
    }
  }
}
```

```
aggs:{
	"aggs_name":{
		"terms":{
			"field": "fieldname【.keyword】",
			"include":[值列表],
			"exclude":[值列表],
			"size": num,
			"order":{"inner_aggs_name": "desc|asc"}
		},
		"range":{
			"field": "fieldname",
		     "ranges":{"from": num, "to": num, "key": name}
		},
		"date_range":{
			"field": "fieldname",
			"ranges":{"from": date, "to": date, "key": name}
		},
		"date_histogram":{
			"field": "birthDay",
			"format": "yyyy",
			"interval": "year"
		}
		aggs:{}
	}
}
```



## 1. Bucket

### 1.1 **terms** 分组聚合

按字段分组并降序返回账户数量最多的十条数据

```
GET /bank/_search?pretty
{
  "size": 1,
  "aggs": {
    "ss": {
      "terms": {
        "size": 1,  # 不写默认返回10条
        "field": "state.keyword"
      }
    }
  }
}
```

### 1.2 histogram 条形图桶

```json
# 以价格间隔20000为区间分组，每个分组下price相加
GET /cars/transactions/_search
{
   "size" : 0,
   "aggs":{
      "price":{
         "histogram":{ 
            "field": "price",
            "interval": 20000
         },
         "aggs":{
            "revenue": {
               "sum": { 
                 "field" : "price"
               }
             }
         }
      }
   }
}
```

### 1.3 date-histogram 按时间统计

```json
# 每月销售了几台汽车
	# interval: 时间间隔
	# format：格式化日期
	# "min_doc_count" : 0 返回空桶
    #       "extended_bounds" : {  强制返回整年
                "min" : "2014-01-01",
                "max" : "2014-12-31"
            }
GET /cars/transactions/_search
{
   "size" : 0,
   "aggs": {
      "sales": {
         "date_histogram": {
            "field": "sold",
            "interval": "month", 
            "format": "yyyy-MM-dd" 
         }
      }
   }
}
```

### 1.4 全局桶

```json
# 福特汽车与 所有 汽车平均售价的比较
# single_avg_price 度量计算是基于查询范围内所有文档，即所有 福特 汽车
# avg_price 度量是嵌套在  全局 桶下的，这意味着它完全忽略了范围并对所有文档进行计算
{
    "size" : 0,
    "query" : {
        "match" : {
            "make" : "ford"
        }
    },
    "aggs" : {
        "single_avg_price": {
            "avg" : { "field" : "price" } 
        },
        "all": {
            "global" : {}, # global 全局桶没有参数。avg_price聚合操作针对所有文档，忽略汽车品牌
            "aggs" : {
                "avg_price": {
                    "avg" : { "field" : "price" } 
                }

            }
        }
    }
}
```

### 1.5 过滤桶

 **对聚合后的结果进行过滤**

```sense
{
   "size" : 0,
   "query":{
      "match": {
         "make": "ford"
      }
   },
   "aggs":{
      "recent_sales": {
         "filter": { 
            "range": {
               "sold": {
                  "from": "now-1M"
               }
            }
         },
         "aggs": {
            "average_price":{
               "avg": {
                  "field": "price" 
               }
            }
         }
      }
   }
}
```

### 1.6 Range/Date_Range Aggregation 范围分组聚合

NBA球员年龄按20,20-35,35这样分组

```
POST /nba/_search
{
	"aggs": {
		"ageRange": {
			"range": {
				"field": "age",
				"ranges": [{
						"to": 20,
						"key": "A"
					},
					{
						"from": 20,
						"to": 35,
						"key": "B"
					},
					{
						"from": 35,
						"key": "C"
					}
				]
			}
		}
	},
	"size": 0
}
```

## 2. Metrics

### 2.1  **avgs**

```
GET /bank/_search?pretty
{
  "size": 0,
  "aggs": {
    "group_by_state": {
      "terms": {
        "field": "state.keyword",
        "order": {                             通过指定terms聚合内的顺序来使用嵌套聚合的结果进行排序，而不是按计数对结果进行排序
          "average_balance": "desc"
        }
      },
      "aggs": {
        "average_balance": {
          "avg": {
            "field": "balance"
          }
        }
      }
    }
  }
}
```

### 2.2 extended_stats

可以对聚合的结果进行更近一步的分析 ，常见的 count sum avg min max 等都可以一目了然

```
             "stats": {
23             "count": 3,
24             "min": 10000,
25             "max": 20000,
26             "avg": 16666.666666666668,
27             "sum": 50000,
28             "sum_of_squares": 900000000,
29             "variance": 22222222.22222221,
30             "std_deviation": 4714.045207910315,
31             "std_deviation_bounds": {
32               "upper": 26094.757082487296,
33               "lower": 7238.5762508460375
34             }
```

### 2.3 筛选分组聚合 include、exclude

湖⼈和⽕箭队按球队平均年龄进⾏分组排序 (指定值列表)

```
POST /nba/_search
{
	"aggs": {
		"aggsTeamName": {
			"terms": {
				"field": "teamNameEn",
				"include": ["Lakers", "Rockets", "Warriors"],  支持正则表达式"include": "Lakers|Ro.*|Warriors.*",
				"exclude": ["Warriors"],
				"size": 30,
				"order": {
					"avgAge": "desc"
				}
			},
			"aggs": {
				"avgAge": {
					"avg": {
						"field": "age"
					}
				}
			}
		}
	},
	"size": 0
}
```

## 3. post_filter

对搜索结果进行过滤

```json
# 高版本已过期
{
    "size" : 0,
    "query": {
        "match": {
            "make": "ford"
        }
    },
    "post_filter": {    
        "term" : {
            "color" : "green"
        }
    },
    "aggs" : {
        "all_colors": {
            "terms" : { "field" : "color" }
        }
    }
}
```

## 4. 排序

### 4.1 内置排序

>聚合结果默认按`doc_count`降序排序

- `_count`	按文档数排序。对 `terms` 、 `histogram` 、 `date_histogram` 有效。
- ` _term`      按词项的字符串值的字母顺序排序。只在 `terms` 内使用。
- ` _key`        按每个桶的键值数值排序（理论上与 _term 类似）。 只在 `histogram` 和 date_histogram` 内使用。

```json

{
    "size" : 0,
    "aggs" : {
        "colors" : {
            "terms" : {
              "field" : "color",
              "order": {
                "_count" : "asc" 
              }
            }
        }
    }
}
```

### 4.2 按指标排序

```json
{
    "size" : 0,
    "aggs" : {
        "colors" : {
            "terms" : {
              "field" : "color",
              "order": {
                "avg_price" : "asc" 
                # extended_stats返回多值可以使用关键词排序： stats.variance 
              }
            },
            "aggs": {
                "avg_price": {
                    "avg": {"field": "price"} 
                }
            }
        }
    }
}
```

### 4.3 基于深度指标排序

只作用于单值桶：`filter` `global` `reverse_nested`

```sense
GET /cars/transactions/_search
{
    "size" : 0,
    "aggs" : {
        "colors" : {
            "histogram" : {
              "field" : "price",
              "interval": 20000,
              "order": {
                "red_green_cars>stats.variance" : "asc" 
              }
            },
            "aggs": {
                "red_green_cars": {
                    "filter": { "terms": {"color": ["red", "green"]}}, 
                    "aggs": {
                        "stats": {"extended_stats": {"field" : "price"}} 
                    }
                }
            }
        }
    }
}
```

## 5. 近似聚合

**精确 + 实时**

数据可以存入单台机器的内存之中，我们可以随心所欲，使用任何想用的算法。结果会 100% 精确，响应会相对快速。

**大数据 + 精确**

传统的 Hadoop。可以处理 PB 级的数据并且为我们提供精确的答案，但它可能需要几周的时间才能为我们提供这个答案。

**大数据 + 实时**

近似算法为我们提供准确但不精确的结果。 es采用

### 5.1 去重 cardinality

```json
# precision_threshold控制精度
  #  接受 0–40,000 之间的数字，更大的值还是会被当作 40,000 来处理
示例会确保当字段唯一值在 100 以内时会得到非常准确的结果。尽管算法是无法保证这点的，但如果基数在阈值以下，几乎总是 100% 正确的。高于阈值的基数会开始节省内存而牺牲准确度，同时也会对度量结果带入误差。
GET /cars/transactions/_search
{
    "size" : 0,
    "aggs" : {
        "distinct_colors" : {
            "cardinality" : {
              "field" : "color"，
              "precision_threshold" : 100 
            }
        }
    }
}
```

### 5.2 百分比

**percentiles**

```json
# 默认返回百分位数值 [1, 5, 25, 50, 75, 95, 99] 
GET /website/logs/_search
{
    "size" : 0,
    "aggs" : {
        "load_times" : {
            "percentiles" : {
                "field" : "latency" ,
                "percents" : [50, 95.0, 99.0] # 自己指定返回百分位数值
            }
        }
    }
}
```

 **percentile_ranks**

```json
#  返回的是 分组统计结果在210和800的百分比
#  compression  控制内存与准确度之间的比值。
#     节点越多，准确度越高（同时内存消耗也越大），这都与数据量成正比。
        compression 参数限制节点的最大数目为 20 * compression 。
{
    "size" : 0,
    "aggs" : {
        "zones" : {
            "terms" : {
                "field" : "zone"
            },
            "aggs" : {
                "load_times" : {
                    "percentile_ranks" : {
                      "field" : "latency",
                      "values" : [210, 800],
                       "compression ": 2
                    }
                }
            }
        }
    }
}
```

- 百分位的准确度与百分位的 *极端程度* 相关，也就是说 1 或 99 的百分位要比 50 百分位要准确。这只是数据结构内部机制的一种特性，但这是一个好的特性，因为多数人只关心极端的百分位。
- 对于数值集合较小的情况，百分位非常准确。如果数据集足够小，百分位可能 100% 精确。
- 随着桶里数值的增长，算法会开始对百分位进行估算。它能有效在准确度和内存节省之间做出权衡。 不准确的程度比较难以总结，因为它依赖于 聚合时数据的分布以及数据量的大小。

## 6. 举例

### 6.1 先按颜色分组取每个颜色的平均价格，然后对每个颜色下的品牌分组，求每个品牌的最低和最高价格

```sense
{
   "size" : 0,
   "aggs": {
      "colors": {
         "terms": {
            "field": "color"
         },
         "aggs": {
            "avg_price": { "avg": { "field": "price" }
            },
            "make" : {
                "terms" : {
                    "field" : "make"
                },
                "aggs" : { 
                    "min_price" : { "min": { "field": "price"} }, 
                    "max_price" : { "max": { "field": "price"} } 
                }
            }
         }
      }
   }
}
```













