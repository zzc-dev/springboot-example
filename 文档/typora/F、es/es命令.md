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



# es查询（各字段解释）

es中的查询请求有两种方式，一种是简易版的查询，另外一种是使用JSON完整的请求体，叫做结构化查询（DSL）。
由于DSL查询更为直观也更为简易，所以大都使用这种方式。
DSL查询是POST过去一个json，由于post的请求是json格式的，所以存在很多灵活性，也有很多形式。
这里有一个地方注意的是官方文档里面给的例子的json结构只是一部分，并不是可以直接黏贴复制进去使用的。一般要在外面加个query为key的机构。

## 1. match

> **匹配字段会进行分词，只要包含分词字段就匹配**

查询和"我的宝马多少马力"这个查询语句匹配的文档。

```
{
  "query": {
    "match": {
        "content" : {
            "query" : "我的宝马多少马力"
        }
    }
  }
}
```

上面的查询匹配就会进行分词，比如"宝马多少马力"会被分词为"宝马 多少 马力", 所有有关"宝马 多少 马力", 那么所有包含这三个词中的一个或多个的文档就会被搜索出来。
并且根据lucene的评分机制(TF/IDF)来进行评分。

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

## 3. multi_match

> 如果我们希望两个字段进行匹配，其中一个字段有这个文档就满足的话，使用multi_match

```
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
>  如：完全匹配"宝马 发动机"的文档评分会比较靠前，如果只匹配宝马的文档评分乘以0.3的系数

```
{
  "query": {
    "multi_match": {
      "query": "我的宝马发动机多少",
      "type": "best_fields",
      "fields": [
        "tag",
        "content"
      ],
      "tie_breaker": 0.3
    }
  }
}
```

### 3.2 most_fields

> 越多字段匹配的文档评分越高

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

## 6. bool联合查询

- `must`: 文档 *必须* 匹配这些条件才能被包含进来
- `must_not`: 文档 *必须不* 匹配这些条件才能被包含进来
- `should`:  如果满足这些语句中的任意语句，将增加 `_score` ，否则，无任何影响。它们主要用于修正每个文档的相关性得分。

## 7. filter

**过滤查询（Filtering queries）**

​		只是简单的检查包含或者排除，这就使得计算起来非常快

​		结果会被缓存到内存中以便快速读取

## 8. range

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

# es聚合

## 1. **terms** 分组聚合

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

## 2. **avgs**

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

## 3. 筛选分组聚合 include、exclude

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

## 4. Range/Date_Range Aggregation 范围分组聚合

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

## 5. Date Histogram Aggregation 时间柱状图聚合

- 按天、⽉、年等进⾏聚合统计。可按 year (1y), quarter (1q), month (1M), week (1w), day(1d), hour (1h), minute (1m), second (1s) 间隔聚合
- NBA球员按出⽣年分组

```
POST /nba/_search
{
	"aggs": {
		"birthday_aggs": {
			"date_histogram": {
				"field": "birthDay",
				"format": "yyyy",
				"interval": "year"
			}
		}
	},
	"size": 0
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



聚合值过滤

```
{
    "size": 0,
    "aggs": {
        "terms": {
            "terms": {
                "field": "mustTags",
                "include": ".*总则.*",
                "size": 999
            }
        }
    }
}
```















