1. es可以从集群中的任何节点获取数据

2. 批量索引文档，减少网络IO。最佳批处理大小取决于：文档大小和复杂性、集群资源的可用性。
   建议：1000-5000个文档，有效负载5M-10M

3. es的分片故障处理：主分片所在节点故障如何处理

4. es的节点分类

   



响应体：

- `took` – Elasticsearch运行查询多长时间（以毫秒为单位）
- `timed_out` –搜索请求是否超时
- `_shards` –搜索了多少个分片，以及成功，失败或跳过了多少个分片。
- `max_score` –找到的最相关文件的分数
- `hits.total.value` -找到了多少个匹配的文档
- `hits.sort` -文档的排序位置（不按相关性得分排序时）
- `hits._score`-文档的相关性得分（使用时不适用`match_all`）



match：分词查询，匹配包含tom和jane的所有文档

match_phrase：将tom jane当成一个词组查询，匹配包含“tom jane”的文档

term：不分词，精确查询，下面语句查询不到，需将name改为name.keyword

```
post /users/_doc/_search
{
  "query":{
    "match": {
      "name": "tom jane"
    }
  }
}
```

### 配置文件位置

Elasticsearch具有三个配置文件：

- `elasticsearch.yml` 用于配置Elasticsearch
- `jvm.options` 用于配置Elasticsearch JVM设置
- `log4j2.properties` 用于配置Elasticsearch日志记录



1个索引有多个分片，每个分片有多个副本。

对分片的更新必须同步到副本中。

**数据复制模型：** 分片副本保持同步并从中读取内容的过程。

es的数据复制模型基于 *主备份模型*

> 该模型基于副本组中的一个副本作为主分片。其他副本称为*副本碎片*。主分片充当所有索引操作的主要入口点。它负责验证它们并确保它们是正确的。一旦主分片接受了索引操作，主分片还负责将操作复制到其他副本

主分片遵循以下基本流程：

1. 验证语句结构的正确性，不正确拒绝
2. 在本地执行操作，即索引或删除相关文档。这还将验证字段的内容并在需要时拒绝。
3. 将操作转发到当前同步副本集中的每个副本。如果有多个副本，则这是并行完成的。
4. 一旦所有副本都成功执行了操作并响应了主服务器，主服务器便会向客户端确认请求的成功完成

# document api

## 1. 索引API

```console
PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "twitter,index10,-index1*,+ind*" 
    }
}

PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "false" 
    }
}

PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "true" 
    }
}
```

`write.wait_for_active_shards`: 索引请求返回前需要等待多少个分片写入成功，默认是1，只要主分片写入成功就返回

`index.refresh_interval`  索引数据提交到刷新成新段的间隔，默认是一秒

`refresh`

- false  每一秒刷新一次即最长需要等待一秒才可见，其实是一秒生成了一个新段，这是默认值，这样做的目的是避免生成过多的段，增加合并的成本，这个是异步方式
- true 写入请求同时刷新数据到新段，刷新是请求执行的一部分，所以只要请求返回数据立即可见，会增加CPU开销，这个可以实现同步写入
- wait_for 这种方式可以理解为折衷的方式，写入请求阻塞到数据刷新动作的发生，写入请求最长需等待一秒，这个也实现了同步写入

## 2. get api

**实时性**

​	get API默认是实时的，不受索引刷新率的影响（当数据对搜索可见时）。如果文档已更新但尚未刷新，get API将就地发出刷新调用以使文档可见。自上次刷新以来，这还将使其他文档发生更改。

```console
禁用 realtime: false
```

**source filter**

```
GET twitter/_doc/0?_source=false
GET twitter/_doc/0?_source_includes=*.id&_source_excludes=entities
GET twitter/_doc/0?_source=*.id,retweeted
```

**分散式**

​	get操作将散列到特定的分片中。然后将其重定向到该分片的副本之一，并返回结果。这意味着我们拥有的副本越多，我们将拥有越好的GET缩放比例。

​    `preference`

​             _local： 如果可能，该操作将首选在本地分配的分片上执行 

​             Custom(string) value : 确保相同的副本处理相同的自定义值       

在内部，Elasticsearch将旧文档标记为已删除，并添加了一个全新的文档。旧版本的文档不会立即消失，尽管您将无法访问它。当您继续索引更多数据时，Elasticsearch会在后台清理已删除的文档。

## 3. delete api

**乐观并发控制**

删除操作可以是有条件的，并且只有在对文档的最后修改被分配了由`if_seq_no`和`if_primary_term`参数指定的序号和主要术语的情况下才能执行。如果检测到不匹配，则该操作将产生`VersionConflictException` 和状态码409。有关更多详细信息，请参见[*乐观并发控制*](https://www.elastic.co/guide/en/elasticsearch/reference/7.0/optimistic-concurrency-control.html)。

**版本控制**

索引的每个文档都经过版本控制。删除文档时，`version`可以指定，以确保我们要删除的相关文档实际上已被删除，同时它也没有更改。在文档上执行的每个写操作（包括删除操作）都会导致其版本增加。删除后的文档版本号在短时间内仍然可用，以便控制并发操作。删除的文档版本保持可用状态的时间长度由`index.gc_deletes`索引设置确定，默认为60秒

**超时**

当执行删除操作时，分配给执行删除操作的主分片可能不可用。造成这种情况的某些原因可能是主分片当前正在从存储中恢复或正在进行重定位。默认情况下，删除操作将等待主碎片最多可用1分钟，然后失败并响应错误。该`timeout`参数可用于显式指定其等待时间。这是将其设置为5分钟的示例：

```console
DELETE /twitter/_doc/1?timeout=5m
```

## 4. delete by query

在`_delete_by_query`执行期间，顺序执行多个搜索请求，以找到所有要删除的匹配文档。每次找到一批文档时，都会执行相应的批量请求以删除所有这些文档。如果搜索或批量请求被拒绝，则`_delete_by_query` 依靠默认策略重试被拒绝的请求（最多10次，并以指数方式退回）。达到最大重试次数限制将导致`_delete_by_query` 中止，并且所有失败都将在`failures`的响应中返回。已经执行的删除仍然会保留。换句话说，该过程不会回滚，只会中止。当第一个失败导致中止时，失败的批量请求返回的所有失败都将在`failures` 元素; 因此，可能会有很多失败的实体

如果您想计算版本冲突而不是使它们中止，请`conflicts=proceed`在url或`"conflicts": "proceed"`请求正文中进行设置

```console
POST twitter/_delete_by_query?conflicts=proceed
{
  "query": {
    "match_all": {}
  }
}
```

默认情况下，`_delete_by_query`使用滚动批处理1000。您可以使用`scroll_size`URL参数更改批处理大小

```console
POST twitter/_delete_by_query?scroll_size=5000
{
  "query": {
    "term": {
      "user": "kimchy"
    }
  }
}
```

