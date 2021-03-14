https://www.elastic.co/guide/cn/elasticsearch/guide/current/_an-empty-cluster.html  es2.x官方中文文档

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

# 基础

## 1. 和es交互

### Java API

如果你正在使用 Java，在代码中你可以使用 Elasticsearch 内置的两个客户端：

- **节点客户端（Node client）**

  节点客户端作为一个非数据节点加入到本地集群中。换句话说，它本身不保存任何数据，但是它知道数据在集群中的哪个节点中，并且可以把请求转发到正确的节点。

- **传输客户端（Transport client）**

  轻量级的传输客户端可以将请求发送到远程集群。它本身不加入集群，但是它可以将请求转发到集群中的一个节点上。

两个 Java 客户端都是通过 *9300* 端口并使用 Elasticsearch 的原生 *传输* 协议和集群交互。集群中的节点通过端口 9300 彼此通信。如果这个端口没有打开，节点将无法形成一个集群。

Java 客户端作为节点必须和 Elasticsearch 有相同的 *主要* 版本；否则，它们之间将无法互相理解。

### RESTful API with JSON over HTTP

所有其他语言可以使用 RESTful API 通过端口 *9200* 和 Elasticsearch 进行通信，你可以用你最喜爱的 web 客户端访问 Elasticsearch 。事实上，正如你所看到的，你甚至可以使用 `curl` 命令来和 Elasticsearch 交互。

## 2. 面向文档

在应用程序中对象很少只是一个简单的键和值的列表。通常，它们拥有更复杂的数据结构，可能包括日期、地理信息、其他对象或者数组等。

也许有一天你想把这些对象存储在数据库中。使用关系型数据库的行和列存储，这相当于是把一个表现力丰富的对象塞到一个非常大的电子表格中：为了适应表结构，你必须设法将这个对象扁平化—通常一个字段对应一列—而且每次查询时又需要将其重新构造为对象。

Elasticsearch 是 *面向文档* 的，意味着它存储整个对象或 *文档*。Elasticsearch 不仅存储文档，而且 *索引* 每个文档的内容，使之可以被检索。在 Elasticsearch 中，我们对文档进行索引、检索、排序和过滤—而不是对行列数据。这是一种完全不同的思考数据的方式，也是 Elasticsearch 能支持复杂全文检索的原因。

## 3. 集群

ElasticSearch 的主旨是**随时可用和按需扩容**。 而扩容可以通过购买性能更强大（ *垂直扩容* ，或 *纵向扩容* ） 或者数量更多的服务器（ *水平扩容* ，或 *横向扩容* ）来实现。

虽然 Elasticsearch 可以获益于更强大的硬件设备，但是垂直扩容是有极限的。 真正的扩容能力是来自于水平扩容—为集群添加更多的节点，并且将负载压力和稳定性分散到这些节点中。

对于大多数的数据库而言，通常需要对应用程序进行非常大的改动，才能利用上横向扩容的新增资源。 与之相反的是，ElastiSearch天生就是 *分布式的* ，它知道如何通过管理多节点来提高扩容性和可用性。 这也意味着你的应用无需关注这个问题

> 当有节点加入集群中或者从集群中移除节点时，集群将会重新平均分布所有的数据

### 3.1 master

**Master仅仅负责维护集群的状态，并不需要涉及到文档级别的变更和搜索等操作**

1. 创建或删除索引
2. 跟踪哪些节点是集群的一部分
3. 决定将哪些分片分配给哪个节点
4. 等集群范围的操作

上面的一些集群信息, 是由Master节点进行维护, 但是 Master也会把节点信息, 同步给其他节点, 但是只有master节点可以修改

作为用户，我们可以将请求发送到 *集群中的任何节点* ，包括主节点。 **每个节点都知道任意文档所处的位置**，并且能够将我们的请求直接**转发**到存储我们所需文档的节点。 无论我们将请求发送到哪个节点，它都能负责从各个包含我们所需文档的节点收集回数据，并将最终结果返回給客户端。 Elasticsearch 对这一切的管理都是透明的

### 3.2 集群健康状态

```sense
GET /_cluster/health

{
   "cluster_name":          "elasticsearch",
   "status":                "green", 
   "timed_out":             false,
   "number_of_nodes":       1,
   "number_of_data_nodes":  1,
   "active_primary_shards": 0,
   "active_shards":         0,
   "relocating_shards":     0,
   "initializing_shards":   0,
   "unassigned_shards":     0
}
```

`status` 字段指示着当前集群在总体上是否工作正常。它的三种颜色含义如下：

- **`green`**

  所有的主分片和副本分片都正常运行。

- **`yellow`**

  所有的主分片都正常运行，但不是所有的副本分片都正常运行。

- **`red`**

  有主分片没能正常运行。

### 3.3 路由文档到一个分片中

> **shard = hash(routing) % number_of_primary_shards**

索引文档时，如何确定保存的分片：

​	`routing`: 默认是文档的id，也可以设置成一个自定义的值

解释了为什么我们要在创建索引的时候就确定好主分片的数量 并且永远不会改变这个数量：因为如果数量变化了，那么所有之前路由的值都会无效，文档也再也找不到了。

所有的文档 API（ `get` 、 `index` 、 `delete` 、 `bulk` 、 `update` 以及 `mget` ）都接受一个叫做 `routing` 的路由参数 ，通过这个参数我们可以自定义文档到分片的映射。一个自定义的路由参数可以用来确保所有相关的文档——例如所有属于同一个用户的文档——都被存储到同一个分片中

### 3.4 主分片和副本分片的交互

![有三个节点和一个索引的集群](D:\myself\springboot-example\文档\typora\images\es15.png)

我们可以发送请求到集群中的任一节点。 每个节点都有能力处理任意请求。 每个节点都知道集群中任一文档位置，所以可以直接将请求转发到需要的节点上。 在下面的例子中，将所有的请求发送到 `Node 1` ，我们将其称为 ***协调节点(coordinating node)*** 

#### 3.4.1 新建和删除文档

![新建、索引和删除单个文档](D:\myself\springboot-example\文档\typora\images\es16.png)

以下是在主副分片和任何副本分片上面 成功新建，索引和删除文档所需要的步骤顺序：

1. 客户端向 `Node 1` 发送新建或者删除请求。
2. 节点使用文档的 `_id` 确定文档属于分片 0 。请求会被转发到 `Node 3`，因为分片 0 的主分片目前被分配在 `Node 3` 上。
3. `Node 3` 在主分片上面执行请求。如果成功了，它将请求并行转发到 `Node 1` 和 `Node 2` 的副本分片上。一旦所有的副本分片都报告成功, `Node 3` 将向协调节点报告成功，协调节点向客户端报告成功。

**consistency** 一致性

​	在试图执行一个_写_操作之前，主分片都会要求 必须要有 *规定数量(quorum)*（或者换种说法，也即必须要有大多数）的分片副本处于活跃可用状态，才会去执行_写_操作(其中分片副本可以是主分片或者副本分片)。这是为了避免在发生网络分区故障（network partition）的时候进行_写_操作，进而导致数据不一致。

 1. one

    只要主分片ok即可执行写操作

 2. all

    主分片和所有副本分片的状态都没问题才可执行写操作

 3. `quorum` 默认

    大多数的分片副本状态没问题就允许执行_写_操作

    ```
    int( (primary + number_of_replicas) / 2 ) + 1
    ```

  >新索引默认有 `1` 个副本分片，这意味着为满足 `规定数量` *应该* 需要两个活动的分片副本。 但是，这些默认的设置会阻止我们在单一节点上做任何事情。为了避免这个问题，要求只有当 `number_of_replicas` 大于1的时候，规定数量才会执行。

**timeout**    

​	如果没有足够的副本分片会发生什么？ Elasticsearch会等待，希望更多的分片出现。默认情况下，它最多等待1分钟。 如果你需要，你可以使用 `timeout` 参数 使它更早终止： `100` 100毫秒，`30s` 是30秒。

#### 3.4.2 查询一个文档

![取回单个文档](D:\myself\springboot-example\文档\typora\images\es17.png)

1、客户端向 `Node 1` 发送获取请求。

2、节点使用文档的 `_id` 来确定文档属于分片 `0` 。分片 `0` 的副本分片存在于所有的三个节点上。 在这种情况下，它将请求转发到 `Node 2` 。

​	  > **在处理读取请求时，协调结点在每次请求的时候都会通过轮询所有的副本分片来达到负载均衡**。

3、`Node 2` 将文档返回给 `Node 1` ，然后将文档返回给客户端。

在文档被检索时，已经被索引的文档可能已经存在于主分片上但是还没有复制到副本分片。 在这种情况下，副本分片可能会报告文档不存在，但是主分片可能成功返回文档。 一旦索引请求成功返回给用户，文档在主分片和副本分片都是可用的

#### 3.4.3 局部更新文档

![局部更新文档](D:\myself\springboot-example\文档\typora\images\es18.png)

1. 客户端向 `Node 1` 发送更新请求。

2. 它将请求转发到主分片所在的 `Node 3` 。

3. `Node 3` 从主分片检索文档，修改 `_source` 字段中的 JSON ，并且尝试重新索引主分片的文档。 如果文档已经被另一个进程修改，它会重试步骤 3 ，超过 `retry_on_conflict` 次后放弃。

4. 如果 `Node 3` 成功地更新文档，它将新版本的文档并行转发到 `Node 1` 和 `Node 2` 上的副本分片，重新建立索引。 一旦所有副本分片都返回成功， `Node 3` 向协调节点也返回成功，协调节点向客户端返回成功

   <hr/>

   **基于文档的复制**

   当主分片把更改转发到副本分片时， 它不会转发更新请求。 相反，它转发完整文档的新版本。请记住，这些更改将会异步转发到副本分片，并且不能保证它们以发送它们相同的顺序到达。 如果Elasticsearch仅转发更改请求，则可能以错误的顺序应用更改，导致得到损坏的文档。

   <hr/>

### 3.5 分布式检索

搜索需要一种更加复杂的执行模型因为我们不知道查询会命中哪些文档: 这些文档有可能在集群的任何分片上。 一个搜索请求必须询问我们关注的索引（index or indices）的所有分片的某个副本来确定它们是否含有任何匹配的文档。

但是找到所有的匹配文档仅仅完成事情的一半。 在 `search` 接口返回一个 `page` 结果之前，多分片中的结果必须组合成单个排序列表。 为此，搜索被执行成一个两阶段过程，我们称之为 *query then fetch*

#### 3.5.1 query阶段

在初始 *查询阶段* 时， 查询会广播到索引中每一个分片拷贝（主分片或者副本分片）。 每个分片在本地执行搜索并构建一个匹配文档的 *优先队列*。

![æ¥è¯¢è¿ç¨åå¸å¼æç´¢](D:\myself\springboot-example\文档\typora\images\es20.png)

1. 客户端发送一个 `search` 请求到 `Node 3` ， `Node 3` 会创建一个大小为 `from + size` 的空优先队列。
2. `Node 3` 将查询请求转发到索引的每个主分片或副本分片中。每个分片在本地执行查询并添加结果到大小为 `from + size` 的本地有序优先队列中。
3. 每个分片返回各自优先队列中**所有文档的 ID 和排序值**给协调节点，也就是 `Node 3` ，它合并这些值到自己的优先队列中来产生一个全局排序后的结果列表。

#### 3.5.2 fetch阶段

​	查询阶段标识哪些文档满足搜索请求，但是我们仍然需要取回这些文档

![åå¸å¼æç´¢çååé¶æ®µ](D:\myself\springboot-example\文档\typora\images\es21.png)

1. 协调节点辨别出哪些文档需要被取回并向相关的分片提交多个 `GET` 请求。
2. 每个分片加载并 *丰富* 文档，如果有需要的话，接着返回文档给协调节点。
3. 一旦所有的文档都被取回了，协调节点返回结果给客户端。

  协调节点给持有相关文档的每个分片创建一个 [multi-get request](https://www.elastic.co/guide/cn/elasticsearch/guide/current/distrib-multi-doc.html) ，并发送请求给同样处理查询阶段的分片副本

#### 3.5.3 深分页问题（Deep Pagination）

先查后取的过程支持用 `from` 和 `size` 参数分页，但是这是 *有限制的* 。 要记住需要传递信息给协调节点的每个分片必须先创建一个 `from + size` 长度的队列，协调节点需要根据 `number_of_shards * (from + size)` 排序文档，来找到被包含在 `size` 里的文档。

取决于你的文档的大小，分片的数量和你使用的硬件，给 10,000 到 50,000 的结果文档深分页（ 1,000 到 5,000 页）是完全可行的。但是使用足够大的 `from` 值，排序过程可能会变得非常沉重，使用大量的CPU、内存和带宽。因为这个原因，我们强烈建议你不要使用深分页。

实际上， “深分页” 很少符合人的行为。当2到3页过去以后，人会停止翻页，并且改变搜索标准。会不知疲倦地一页一页的获取网页直到你的服务崩溃的罪魁祸首一般是机器人或者web spider。

如果你 *确实* 需要从你的集群取回大量的文档，你可以通过用 `scroll` 查询禁用排序使这个取回行为更有效率

#### 3.5.4 参数

`preference`

​	用来控制由哪些分片或节点来处理搜索请求,可以避免 *bouncing results* 问题

 >**Bouncing Results**
 >
 >想象一下有两个文档有同样值的时间戳字段，搜索结果用 `timestamp` 字段来排序。 由于搜索请求是在所有有效的分片副本间轮询的，那就有可能发生主分片处理请求时，这两个文档是一种顺序， 而副本分片处理请求时又是另一种顺序。
 >
 >这就是所谓的 *bouncing results* 问题: 每次用户刷新页面，搜索结果表现是不同的顺序。 让同一个用户始终使用同一个分片，这样可以避免这种问题， 可以设置 `preference` 参数为一个特定的任意值比如用户会话ID来解决。

`timeout`    

​	分片允许处理数据的最大时间。如果没有足够的时间处理所有数据，这个分片的结果可以是部分的，甚至是空数据。

>  ```
> 注意，有时超时仍然是一个最有效的操作，知道这一点很重要； 很可能查询会超过设定的超时时间。这种行为有两个原因：
> 
> 	超时检查是基于每文档做的。 但是某些查询类型有大量的工作在文档评估之前需要完成。 这种 "setup" 阶段并不考虑超时设置，所以太长的建立时间会导致超过超时时间的整体延迟。
> 	因为时间检查是基于每个文档的，一次长时间查询在单个文档上执行并且在下个文档被评估之前不会超时。 这也意味着差的脚本（比如带无限循环的脚本）将会永远执行下去。
>  ```

`routing`

  在[路由文档到一个分片中](#3.3 路由文档到一个分片中)中，可以指定路由参数将文档保存到指定的分片，因此在查询时也可以指定路由参数去指定分片查询

`search_type`

​	默认：`query_then_fetch`

   		`dfs_query_then_fetch` 改善相关性精确度, 有预查询阶段，这个阶段可以从所有相关分片获取词频来计算全局词频

```js
GET /_search?search_type=dfs_query_then_fetch
```

#### 3.5.5 游标查询 Scroll

​	`scroll` 查询 可以用来对 Elasticsearch 有效地执行大批量的文档查询，而又不用付出深度分页那种代价。

 	游标查询会取某个时间点的快照数据。 查询初始化之后索引上的任何变化会被它忽略。

​	深度分页的代价根源是结果集全局排序，如果去掉全局排序的特性的话查询结果的成本就会很低。 游标查询用字段 `_doc` 来排序。 这个指令让 Elasticsearch 仅仅从还有结果的分片返回下一批结果。

```js
# 每次游标查询窗口一分钟
GET /old_index/_search?scroll=1m 
{
    "query": { "match_all": {}},
    "sort" : ["_doc"], 
    "size":  1000
}
```

​	尽管我们指定字段 `size` 的值为1000，我们有可能取到超过这个值数量的文档。 当查询的时候， 字段 `size` 作用于单个分片，所以每个批次实际返回的文档数量最大为`size * number_of_primary_shards` 。

### 3.6 节点类型

#### 3.6.1 协调节点

当一个搜索请求被发送到某个节点时，这个节点就变成了协调节点。 这个节点的任务是广播查询请求到所有相关分片并将它们的响应整合成全局排序后的结果集合，这个结果集合会返回给客户端。

### 3.7 分片内部原理

#### 3.7.1 近实时搜索

通过增加新的补充索引来反映新近的修改，而不是直接重写整个倒排索引

Elasticsearch 基于 Lucene, 这个 java 库引入了 *按段搜索* 的概念。

>ES索引（逻辑概念）= 多个分片（物理概念）
>
>1个分片  = 1个Lucene索引 => 多个段Segment+ Commit point



<img src="D:\myself\springboot-example\文档\typora\images\es22.png" alt="A Lucene index with a commit point and three segments" style="zoom:50%;" />

提交（Commiting）一个新的段到磁盘需要一个 [`fsync`](http://en.wikipedia.org/wiki/Fsync) 来确保段被物理性地写入磁盘，这样在断电的时候就不会丢失数据。 但是 `fsync` 操作代价很大; 如果每次索引一个文档都去执行一次的话会造成很大的性能问题。

  1. 新的文档被写入内存索引缓冲区`In-memory-buffer`和`translog`

     <img src="D:\myself\springboot-example\文档\typora\images\es23.png" alt="New documents are added to the in-memory buffer and appended to the transaction log" style="zoom:67%;" />

  2. `refresh`

     **写入和打开一个新段的轻量的过程叫做 *refresh* 。 默认情况下每个分片会每秒自动刷新一次。**

     这就是为什么我们说 Elasticsearch 是 ***近* 实时搜索: 文档的变化并不是立即对搜索可见，但会在一秒之内变为可见**。

     每隔1s将`In-memory buffer`的文档写入到一个新段，并将新段写到内存缓冲区

     **刷新（refresh）完成后,` In-memory buffer`被清空但是`TransLog`不会**

     ```json
     # 刷新（Refresh）所有的索引。
     POST /_refresh 
     # 只刷新（Refresh） blogs 索引。
     POST /blogs/_refresh 
     ```

     <img src="D:\myself\springboot-example\文档\typora\images\es24.png" alt="After a refresh, the buffer is cleared but the transaction log is not" style="zoom:67%;" />

     ```json
     # 优化索引速度而不是近实时搜索， 可以通过设置 `refresh_interval` ， 降低每个索引的刷新频率：
     PUT /my_logs
     {
       "settings": {
         "refresh_interval": "30s" 
       }
     }
     
     # 在生产环境中，当你正在建立一个大的新索引时，可以先关闭自动刷新，待开始使用该索引时，再把它们调回来：
     PUT /my_logs/_settings
     { "refresh_interval": -1 } 
     
     PUT /my_logs/_settings
     { "refresh_interval": "1s" } 
     ```

		3. 这个进程继续工作，更多的文档被添加到内存缓冲区和追加到事务日志

  4. `flush`

     每隔一段时间—例如 translog 变得越来越大—索引被刷新（flush）；一个新的 translog 被创建，并且一个全量提交被执行

      **在刷新（flush）之后，段被全量提交，并且事务日志被清空**

     - 所有在内存缓冲区的文档都被写入一个新的段。
     - 缓冲区被清空。
     - 一个提交点被写入硬盘。
     - 文件系统缓存通过 `fsync` 被刷新（flush）。
     - 老的 translog 被删除

#### 3.7.2 translog

​	如果没有用 `fsync` 把数据从文件系统缓存刷（flush）到硬盘，我们不能保证数据在断电甚至是程序正常退出之后依然存在。为了保证 Elasticsearch 的可靠性，需要确保数据变化被持久化到磁盘

​	Elasticsearch 增加了一个 *translog* ，或者叫事务日志，在每一次对 Elasticsearch 进行操作时均进行了日志记录。

​	translog 提供所有还没有被刷到磁盘的操作的一个持久化纪录。当 Elasticsearch 启动的时候， 它会从磁盘中使用最后一个提交点去恢复已知的段，并且会重放 translog 中所有在最后一次提交后发生的变更操作。

​	translog 也被用来提供实时 CRUD 。当你试着**通过ID**查询、更新、删除一个文档，它会在尝试从相应的段中检索之前， 首先检查 translog 任何最近的变更。这意味着它总是能够实时地获取到文档的最新版本。

> 通过ID 实时CRUID

#### 3.7.3 flush API

​	这个执行一个提交并且截断 translog 的行为在 Elasticsearch 被称作一次 *flush* 。

​    分片每30分钟被自动刷新（flush），或者在 translog 太大的时候也会刷新。

```json
# 	刷新（flush） blogs 索引。
POST /blogs/_flush 
# 刷新所有的索引并等待完成
POST /_flush?wait_for_ongoing 
```

#### 3.7.4 段合并

​	由于自动刷新流程每秒会创建一个新的段 ，这样会导致短时间内的段数量暴增。而段数目太多会带来较大的麻烦。 每一个段都会消耗文件句柄、内存和cpu运行周期。更重要的是，每个搜索请求都必须轮流检查每个段；所以段越多，搜索也就越慢。

​    **段合并的时候会将那些旧的已删除文档从文件系统中清除**。

​    启动段合并不需要你做任何事。进行索引和搜索时会自动进行。

   **optimize API**

​		`optimize` API大可看做是 *强制合并* API。它会将一个分片强制合并到 `max_num_segments` 参数指定大小的段数目。 这样做的意图是减少段的数量（通常减少到一个），来提升搜索性能。

```
optimize API 不应该 被用在一个活跃的索引————一个正积极更新的索引。后台合并流程已经可以很好地完成工作。 optimizing 会阻碍这个进程。不要干扰它！
```

在特定情况下，使用 `optimize` API 颇有益处。例如在日志这种用例下，每天、每周、每月的日志被存储在一个索引中。 老的索引实质上是只读的；它们也并不太可能会发生变化。

在这种情况下，使用optimize优化老的索引，将每一个分片合并为一个单独的段就很有用了；这样既可以节省资源，也可以使搜索更加快速：

```json
POST /logstash-2014-10/_optimize?max_num_segments=1 
```

请注意，使用 `optimize` API 触发段合并的操作不会受到任何资源上的限制。这可能会消耗掉你节点上全部的I/O资源, 使其没有余裕来处理搜索请求，从而有可能使集群失去响应。 如果你想要对索引执行 `optimize`，你需要先使用分片分配把索引移到一个安全的节点，再执行。

### 3.8 集群管理

#### 3.8.1 集群状态

```json
# 查看集群的健康状况
GET _cluster/health

# 获取每个索引的细节（状态，分片数，未分片分片数）
GET _cluster/health?level=indices

# 阻塞等待状态变化
GET _cluster/health?wait_for_status=green
```

  ```json
# 监控单个节点
# https://www.elastic.co/guide/cn/elasticsearch/guide/current/_monitoring_individual_nodes.html
GET _nodes/stats
  # 索引部分
  # 操作系统和进程部分
  # JVM部分
  # 线程池部分
	  "index": {
     	"threads": 1,
     	"queue": 0,
     	"active": 0,
     	"rejected": 0,
     	"largest": 1,
     	"completed": 1
  	 }
		如果队列中任务单元数达到了极限，新的任务单元会开始被拒绝，你会在 rejected 统计值上看到它反映出来。这通常是你的集群在某些资源上碰到瓶颈的信号。因为		队列满意味着你的节点或集群在用最高速度运行，但依然跟不上工作的蜂拥而入。
  # 文件系统和网络部分
  # 断路器

# 集群统计 类似于节点统计，区别是对于单个指标，展示的是所有节点的总和
get _cluster/stats

# 索引统计
get my_index/_stats

# cat api
GET /_cat #获取所有可用的cat命令
  ```

**等待中的任务**

```js
GET _cluster/pending_tasks
```

主节点很少会成为集群的瓶颈。唯一可能成为瓶颈的是集群状态非常大 *而且* 更新频繁。

解决方案：

- 使用一个更强大的主节点。不幸的是，这种垂直扩展只是延迟这种必然结果出现而已。
- 通过某些方式限定文档的动态性质来限制集群状态的大小。
- 到达某个阈值后组建另外一个集群

#### 3.8.2 防止脑裂

>*脑裂* ，一种两个主节点同时存在于一个集群的现象

```yaml
# 等有2个master候选节点时再选取主节点
# 一般设置 master候选节点/2 + 1 半数机制 
discovery.zen.minimum_master_nodes: 2 
```

上面是集群的配置文件，当集群中master候选节点动态变动时，你不得不修改每一个索引节点的配置并且重启你的整个集群只是为了让配置生效。

```js
# 动态修改
PUT /_cluster/settings
{
    "persistent" : {
        "discovery.zen.minimum_master_nodes" : 2
    }
}
```

#### 3.8.3 集群恢复方面的配置

**集群恢复**

​	假设有10个节点，有10分片（5主1副本）均匀地分配在每个节点

	1. 有5个节点意外断开，或者当你重启你的集群，恰巧出现了 5 个节点已经启动，还有 5 个还没启动的场景。
 	2. 另外5个节点相互通信，选取出一个master，然后启动**分片复制**
 	3. 过会另外5个节点加入集群，发现他们的数据正在复制到其他节点，需要删除本地的分片数据（多余或者过时了）。
 	4. 整个集群又重新平衡，分配分片。

在整个过程中，你的节点会消耗磁盘和网络带宽，来回移动数据，因为没有更好的办法。对于有 TB 数据的大集群, 这种无用的数据传输需要 *很长时间* 。如果等待所有的节点重启好了，整个集群再上线，所有的本地的数据都不需要移动。

**解决**

```yaml
# config/elasticsearch.yml 不能动态变更
gateway.recover_after_nodes: 8
gateway.expected_nodes: 10
gateway.recover_after_time: 5m

触发集群恢复
# 1. 集群中至少需要recover_after_nodes个节点
# 2. 集群中有expected_nodes个节点或者等待recover_after_time时间
```

#### 3.8.4 堆内存

>不要触碰这些配置！
>
>​	1. 垃圾回收器
>
>​    2. 线程池

```bash
# Elasticsearch 默认安装后设置的堆内存是 1 GB
# 这里有两种方式修改 Elasticsearch 的堆内存。
export ES_HEAP_SIZE=10g
./bin/elasticsearch -Xmx10g -Xms10g 
```

**把内存的一半给Lucene**

​		标准的建议是把 50％ 的可用内存作为 Elasticsearch 的堆内存，保留剩下的 50％。当然它也不会被浪费，Lucene 会很乐意利用起余下的内存

**不要超过32GB**

​    小于32GB时，Java 使用一个叫作 [内存指针压缩（compressed oops）](https://wikis.oracle.com/display/HotSpotInternals/CompressedOops)的技术。

**关闭swap**

#### 3.8.5 推迟分片分配

1. Node（节点） 19 在网络中失联了（某个家伙踢到了电源线)
2. Master 立即注意到了这个节点的离线，它决定在集群内提拔其他拥有 Node 19 上面的主分片对应的副本分片为主分片
3. 在副本被提拔为主分片以后，master 节点开始执行恢复操作来**重建缺失的副本**。集群中的节点之间互相拷贝分片数据，网卡压力剧增，集群状态尝试变绿。
4. 由于目前集群处于非平衡状态，这个过程还有可能会触发小规模的分片移动。其他不相关的分片将在节点间迁移来达到一个最佳的平衡状态

```js
# 默认情况，集群会等待一分钟来查看节点是否会重新加入，如果这个节点在此期间重新加入，重新加入的节点会保持其现有的分片数据，不会触发新的分片分配。
# 延迟分配不会阻止副本被【提拔为主分片】。集群还是会进行必要的提拔来让集群回到 yellow 状态。【缺失副本的重建】是唯一被延迟的过程。
PUT /_all/_settings 
{
  "settings": {
    "index.unassigned.node_left.delayed_timeout": "5m" 
  }
}
```

**自动取消分片迁移**

​	如果节点在超时后回来，且集群还没有完成分片的移动。

​    es检查主分片的数据和回来的节点的数据一致：

​                一致，master取消正在进行的再平衡并恢复该机器磁盘上的数据（之所以这样做是因为**本地磁盘的恢复永远要比网络间传输要快**）

​                不一致。恢复进程会继续按照正常流程进行。重新加入的节点会删除本地的、过时的数据，然后重新获取一份新的。

#### 3.8.6 滚动升级

常见的原因：Elasticsearch 版本升级，或者服务器自身的一些维护操作（比如操作系统升级或者硬件相关）。不管哪种情况，都要有一种特别的方法来完成一次滚动重启。

1. 可能的话**，停止索引新的数据**。虽然不是每次都能真的做到，但是这一步可以帮助提高恢复速度。

2. **禁止分片分配**。这一步阻止 Elasticsearch 再平衡缺失的分片，直到你告诉它可以进行了。

   ```js
   PUT /_cluster/settings
   {
       "transient" : {
           "cluster.routing.allocation.enable" : "none"
       }
   }
   ```

3. 关闭单个节点。

4. 执行维护/升级。

5. 重启节点，然后确认它加入到集群了。

6. 重启分片分配：

   ```js
   PUT /_cluster/settings
   {
       "transient" : {
           "cluster.routing.allocation.enable" : "all"
       }
   }
   ```

## 4. 索引

必须小写，不能以下划线开头，不能包含逗号

​	`config/elasticsearch.yml`

```
action.auto_create_index: false #关闭自动创建索引
action.destructive_requires_name: true #这个设置使删除只限于特定名称指向的数据, 而不允许通过指定 _all 或通配符来删除指定索引库
```



### 4.1 动名词解释

**索引（名词）：**

​    是一个存储文档的逻辑概念。 *索引* (*index*) 的复数词为 *indices* 或 *indexes* 。

**索引（动词）：**

​    索引一个文档* 就是存储一个文档到一个 *索引* （名词）中以便被检索和查询。这非常类似于 SQL 语句中的 `INSERT` 关键词，除了文档已存在时，新文档会替换旧文档情况之外。

### 4.2 索引与分片

> 索引实际上是指向一个或者多个物理 *分片* 的 *逻辑命名空间*

> Elasticsearch 是利用分片将数据分发到集群内各处的。分片是数据的容器，文档保存在分片内，分片又被分配到集群内的各个节点里。
>
>  **当你的集群规模扩大或者缩小时， Elasticsearch 会自动的在各节点中迁移分片，使得数据仍然均匀分布在集群里**

分片：

 1. 主分片

 2. 副本分片

    一个副本分片只是一个主分片的拷贝。副本分片作为硬件故障时保护数据不丢失的冗余备份，

    并为搜索和返回文档等**读操作**提供服务，索引操作必须是主分片完成

  **在索引建立的时候就已经确定了主分片数，但是副本分片数可以随时修改**。

### 4.3 故障转移

当集群中只有一个节点在运行时，意味着会有一个单点故障问题——没有冗余。 幸运的是，我们只需再启动一个节点即可防止数据丢失

![æ¥æä¸¤ä¸ªèç¹çéç¾¤](D:\myself\springboot-example\文档\typora\images\es13.png)

如果Master故障，集群状态首先变red，然后选取新的主节点，重新分配副本，选取主分片，集群状态变成yellow（此时有副本故障），

red -> yellow提升是瞬间发生的。

### 4.4 水平扩容

> 分片是一个功能完整的搜索引擎，它拥有使用一个节点上的所有资源的能力。

如上图所示：2个节点，3个分片，每个分片一个副本，再次增加一个节点，

​                                                                           **将参数** `number_of_replicas` **调大到 2**

![æ¥æ2ä»½å¯æ¬åç3ä¸ªèç¹çéç¾¤](D:\myself\springboot-example\文档\typora\images\es14.png)

 此时搜索性能提高了1/3，

索引现在拥有9个分片：3个主分片和6个副本分片。 这意味着我们可以将集群扩容到9个节点，每个节点只有一个分片。相比原来3个节点时，集群搜索性能可以提升 *3* 倍。


当然，如果只是在相同节点数目的集群上增加更多的副本分片并不能提高性能，因为每个分片从节点上获得的资源会变少。 你需要增加更多的硬件资源来提升吞吐量。

但是更多的副本分片数提高了数据冗余量：按照上面的节点配置，我们可以在失去2个节点的情况下不丢失任何数据。

## 5. 文档

### 5.1 更新文档

 文档是 *不可改变* 的，不能修改它们。相反，如果想要更新现有的文档，需要 *重建索引* 或者进行替换

​        Elasticsearch 已将旧文档标记为已删除，并增加一个全新的文档。

​		**旧文档并不会马上消失，当继续索引更多的数据，Elasticsearch 会在后台清理这些已删除文档**

​	1. 从旧文档构建 JSON

     		2. 更改该 JSON
     		3. 删除旧文档
     		4. 索引一个新文档

  **更新整个文档**

​	检索并修改它，然后重新索引整个文档

  **部分更新**

​	然而在内部， `update` API 简单使用与之前描述相同的 *检索-修改-重建索引* 的处理过程。 
​    区别在于这个过程发生在分片内部，这样就避免了多次请求的网络开销。
​    通过减少检索和重建索引步骤之间的时间，我们也减少了其他进程的变更带来冲突的可能性。

**冲突**

​	`update` API 在 *检索* 步骤时检索得到文档当前的 `_version` 号，并传递版本号到 *重建索引* 步骤的 `index` 请求。 如果另一个进程修改了处于检索和重新索引步骤之间的文档，那么 `_version` 号将不匹配，更新请求将会失败。

`retry_on_conflict` 返回失败结果前的重试次数

```sense
POST /website/pageviews/1/_update?retry_on_conflict=5 
{
   "script" : "ctx._source.views+=1",
   "upsert": {
       "views": 0
   }
}
```

### 5.2 删除文档

即使文档不存在（ `Found` 是 `false` ）， `_version` 值仍然会增加。这是 Elasticsearch 内部记录本的一部分，用来确保这些改变在跨多节点时以正确的顺序执行

正如已经在[更新文档]中提到的，删除文档不会立即将文档从磁盘中删除，只是将文档标记为已删除状态。随着你不断的索引更多的数据，Elasticsearch 将会在后台清理标记为已删除的文档。

### 5.3 文档冲突与乐观锁

`_version`

 如果该版本不是当前版本号，我们的请求将会失败

**通过外部系统使用版本控制**

​	指定的外部版本号 >= 当前版本号

​	`version_type=external`

```sense
PUT /website/blog/2?version=5&version_type=external
{
  "title": "My first external blog entry",
  "text":  "Starting to get the hang of this..."
}
```

### 5.4 字段类型

`date`

​         JSON中没有date类型，es中的date可以由下面3种方式表示：

​            ①格式化的date字符串，例如"2018-01-01"或者"2018-01-01 12:00:00"

​            ②一个long型的数字，代表从1970年1月1号0点到现在的毫秒数

​            ③一个integer型的数字，代表从1970年1月1号0点到现在的秒数

​      在es内部，date被转为UTC，并被存储为一个长整型数字，代表从1970年1月1号0点到现在的毫秒数

  ```
默认格式：strict_date_optional_time||epoch_millis
  strict_date_optional_time  
  	年份、月份、天必须分别以4位、2位、2位表示，不足两位的话第一位需用0补齐
  	支持："yyyy-MM-dd"、"yyyyMMdd"、"yyyyMMddHHmmss"、"yyyy-MM-ddTHH:mm:ss"、"yyyy-MM-ddTHH:mm:ss.SSS"、"yyyy-MM-ddTHH:mm:ss.SSSZ"
  	不支持："yyyy-MM-dd HH:mm:ss"
  epoch_millis
  	epoch_millis约束值必须大于等于Long.MIN_VALUE，小于等于Long.MAX_VALUE
yyyy-MM-dd HH:mm:ss
	自定义格式
  ```



## 6. 批量操作 _bulk

除了delete没有请求体

每个子请求都是独立执行，因此某个子请求的失败不会对其他子请求的成功与否造成影响。 如果其中任何子请求失败，最顶层的 error 标志被设置为 true ，并且在相应的请求报告出错误明细

这也意味着 `bulk` 请求不是原子的： 不能用它来实现事务控制。每个请求是单独处理的，因此一个请求的成功或失败不会影响其他的请求。

```json
POST /_bulk
{ "delete": { "_index": "website", "_type": "blog", "_id": "123" }}   # 1.要有换行符
{ "create": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "title":    "My first blog post" }
{ "index":  { "_index": "website", "_type": "blog" }}
{ "title":    "My second blog post" }
{ "update": { "_index": "website", "_type": "blog", "_id": "123", "_retry_on_conflict" : 3} }
{ "doc" : {"title" : "My updated blog post"} } # 2.最后一行也要有换行符
```

 **批量操作建议大小**

> 1000-5000个文档并且占用内存5-15M

## 7. mapping

### 7.1 什么是mapping

>定义index的元数据，指定要索引并存储的文档的字段类型

**检索时用到的分析策略, 要和建立索引时的分析策略相同, 否则将导致数据不准确**

(2) ES对不同的类型有不同的存储和检索策略.

> ① 比如: 对full text型的数据类型(如text), 在索引时, 会经过各类处理 (包括分词、normalization(时态转换、同义词转换、大小写转换)等处理), 才会建立到索引数据中.
> ② 再比如: 对exact value(如date), 在索引的分词阶段, 会将整个value作为一个关键词建立到倒排索引中.

### 7.2 mapping的更新

- 映射一旦创建完成, 就不允许修改:

  ​    —— Elasticsearch对文档的分析、存储、检索等过程, 都是严格按照mapping中的配置进行的. 如果允许后期修改mapping, 在检索时对索引的处理将存在不一致的情况, 导致数据检索行为不准确.

- **只能在创建index的时候手动配置mapping, 或者新增field mapping, 但是不能update field mapping.**

## 8. dynamic mapping

### 8.1 什么是动态映射

动态映射时Elasticsearch的一个重要特性: 不需要提前创建iindex、定义mapping信息和type类型, 你可以 **直接向ES中插入文档数据时, ES会根据每个新field可能的数据类型, 自动为其配置type等mapping信息**, 这个过程就是动态映射(dynamic mapping).

Elasticsearch动态映射的示例:

| 字段内容(field) | 映射的字段类型(type) |
| --------------- | -------------------- |
| true \| false   | boolean              |
| 1234            | long                 |
| 123.4           | float                |
| 2018-10-10      | date                 |
| "hello world"   | text                 |

### 8.2 动态映射策略

| 策略     | 功能说明                             |
| -------- | ------------------------------------ |
| `true`   | 开启 —— 遇到陌生字段时, 进行动态映射 |
| `false`  | 关闭 —— 忽略遇到的陌生字段           |
| `strict` | 遇到陌生字段时, 作报错处理           |

```
PUT blog_user
{
  "mappings": {
      "_doc": {
          "dynamic": "strict",			// 严格控制策略
          "properties": {
              "name": { "type": "text" },
              "address": {
                  "type": "object",
                  "dynamic": "true"		// 开启动态映射策略
              }
          }
      }
  }
}
```

### 8.3 添加自定义动态映射模板

 在type中定义动态映射模板(dynamic mapping template) —— 把所有的String类型映射成text和keyword类型

```
PUT blog_user
{
    "mappings": {
        "_doc": {
            "dynamic_templates": [
                {
                    "en": {       // 动态模板的名称
                        "match": "*_en",           // 匹配名为"*_en"的field
                        "match_mapping_type": "string",
                        "mapping": {
                            "type": "text",        // 把所有的string类型, 映射成text类型
                            "analyzer": "english", // 使用english分词器
                            "fields": {
                                "raw": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        }
                    }
                }
            ]
        }
    }
}
```

## 9. 分词器

Elasticsearch 为很多世界流行语言提供良好的、简单的、开箱即用的语言分析器集合。

- 空格拆分单词
- 大写转小写
- 移除常用的*停用词*，如`the`
- 将变型词（例如复数词，过去式）转化为词根, 如foxes` → `fox      

### 9.1 analysis与analyzer

> analysis（文本分析）：把全文本转换为一系列单词（term/token）的过程，也叫分词
>
> analyzer：analysis的实现

### 9.2 analyzer的组成

<img src="D:\myself\springboot-example\文档\typora\images\es19.png" alt="image-20210309171658460" style="zoom: 50%;" />

- Character Filters：处理原始文本，如去除html标签
- Tokenizer（分词器）：按照规则拆分为单词
- Token Filters：将切分的单词进行加工，如：`lowercase`，`stop`删除stopwords，增加同义词等

### 9.3 es内置分词器

- Standard Analyzer：默认分词器，按词切分，小写处理
- Simple Analyzer：安装非字母切分，符号被过滤，小写处理
- Stop Analyzer：小写处理，停用词过滤
- WhiteSpace Analyzer：按空格切分，不转小写
- Keyword Analyzer：不分词
- Pattern Analyzer：正则表达式，默认\\W+（非字符分隔）
- Language：提供30多种常见语言的分词器
- Customer Analyzer：自定义分词器

### 9.4 自定义分词器

```json
PUT /my_index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_english": {
          "type": "english",
          "stem_exclusion": [ "organization", "organizations" ],  # 1. 防止 organization 和 organizations 被缩减为词干
          "stopwords": [                                          # 2. 指定一个自定义停用词列表
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "of", "on", "or", "such", "that",
            "the", "their", "then", "there", "these", "they", "this", "to",
            "was", "will", "with"
          ]
        }
      }
    }
  }
}

```

## 10. 复杂域

### 10.1 多值域

```js
{ "tag": [ "search", "nosql" ]}
```

*数组中所有的值必须是相同数据类型的*

查询时得到的`_source`和插入时顺序是一样的，但实际保存是无序的，不能再查询时指定第一个

### 10.2 空域

在 Lucene 中是不能存储 `null` 值的，所以我们认为存在 `null` 值的域为空域。

```js
"null_value":               null,
"empty_array":              [],
"array_with_null_value":    [ null ]
```

### 10.3 多层级对象

```json
{
    "tweet":            "Elasticsearch is very flexible",
    "user": {    # mapping的type为object
        "id":           "@johnsmith",
        "gender":       "male",
        "age":          26,
        "name": {
            "full":     "John Smith",
            "first":    "John",
            "last":     "Smith"
        }
    }
}
```

**内部对象是如何索引的？**

Lucene 不理解内部对象。 Lucene 文档是由一组键值对列表组成的。为了能让 Elasticsearch 有效地索引内部类，它把我们的文档转化成这样：

```js
{
    "tweet":            [elasticsearch, flexible, very],
    "user.id":          [@johnsmith],
    "user.gender":      [male],
    "user.age":         [26],
    "user.name.full":   [john, smith],
    "user.name.first":  [john],
    "user.name.last":   [smith]
}
```

## 11. 相关性与排序

*相关性得分* 由一个浮点数进行表示，并在搜索结果中通过 `_score` 参数返回， 默认排序是 `_score` 降序

### 11.1 按照字段的值进行排序

`_score` 不被计算, 因为它并没有用于排序。

```sense
GET /_search
{
    "query" : {
        "bool" : {
            "filter" : { "term" : { "user_id" : 1 }}
        }
    },
    "sort": { "date": { "order": "desc" }}
}
```

### 11.2 多级排序

结果首先按第一个条件排序，仅当结果集的第一个 `sort` 值完全相同时才会按照第二个条件进行排序，以此类推。

```sense
GET /_search
{
    "query" : {
        "bool" : {
            "must":   { "match": { "tweet": "manage text search" }},
            "filter" : { "term" : { "user_id" : 2 }}
        }
    },
    "sort": [
        { "date":   { "order": "desc" }},
        { "_score": { "order": "desc" }}
    ]
}
```

### 11.3 多字段值的排序

这些值并没有固有的顺序；一个多值的字段仅仅是多个值的包装，这时应该选择哪个进行排序呢？

对于数字或日期，你可以将多值字段减为单值，这可以通过使用 `min` 、 `max` 、 `avg` 或是 `sum` *排序模式* 。 例如你可以按照每个 `date` 字段中的最早日期进行排序，通过以下方法：

```js
"sort": {
    "dates": {
        "order": "asc",
        "mode":  "min"
    }
}
```

### 11.4 字符串排序

字符串分词后相当于多字段值的排序

如果你想分析一个字符串，如 `fine old art` ， 这包含 3 项。我们很可能想要按第一项的字母排序，然后按第二项的字母排序，诸如此类，但是 Elasticsearch 在排序过程中没有这样的信息

**解决方案**

>同一个字段建立两个索引，一个分词用于搜索，另一个不分词用于排序

```
# title的映射
{
	"title":{
		"type":"text",
		"analyzer": "english",
		"fields":{
			"title.keyword":{
				"type":"keyword"
			}
		}
	}
}

# 排序查询
get /book/_serach
{
	"query":{
		"match":{"title": "fine"}
	},
	"sort":"title.keyword"
}
```

### 11.5 相关性

查询语句会为每个文档生成一个 `_score` 字段。

评分的计算方式取决于查询类型 不同的查询语句用于不同的目的： 

​	`		fuzzy` 查询会计算与关键词的拼写相似程度，

​	`	terms` 查询会计算 找到的内容与关键词组成部分匹配的百分比，

但是通常我们说的 *relevance* 是我们用来计算全文本字段的值相对于全文本检索词相似程度的算法。

>**检索词频TF**
>	检索词在该字段出现的频率？出现频率越高，相关性也越高。
>
>**反向文档频率IDF**
>	每个检索词在索引中出现的频率？频率越高，相关性越低。
>
>**字段长度准则**
>	字段的长度是多少？长度越长，相关性越低。

```json
# explain参数可以帮助查看相关性
GET /_search?explain 
{
   "query"   : { "match" : { "tweet" : "honeymoon" }}
}

"_explanation": { 
   "description": "weight(tweet:honeymoon in 0)
                  [PerFieldSimilarity], result of:",
   "value":       0.076713204,
   "details": [
      {
         "description": "fieldWeight in 0, product of:",
         "value":       0.076713204,
         "details": [
            {  
               "description": "tf(freq=1.0), with freq of:",   #TF
               "value":       1,
               "details": [
                  {
                     "description": "termFreq=1.0",
                     "value":       1
                  }
               ]
            },
            { 
               "description": "idf(docFreq=1, maxDocs=1)", # IDF
               "value":       0.30685282
            },
            { 
               "description": "fieldNorm(doc=0)", # 字段长度准则
               "value":        0.25,
            }
         ]
      }
   ]
}
```

**被破环的相关性**

​	 **低相关性的文档被排在了高相关的文档前**

​	我们在两个主分片上创建了索引和总共 10 个文档，其中 6 个文档有单词 `foo` 。可能是分片 1 有其中 3 个 `foo` 文档，而分片 2 有其中另外 3 个文档，换句话说，所有文档是均匀分布存储的。

​     因为文档是均匀分布存储的，两个分片的 IDF 是相同的。

​	如果有 5 个 `foo` 文档存于分片 1 ，而第 6 个文档存于分片 2 ，在这种场景下， `foo` 在一个分片里非常普通（所以不那么重要），但是在另一个分片里非常出现很少（所以会显得更重要）。这些 IDF 之间的差异会导致不正确的结果。

​	在实际应用中，这并不是一个问题，本地和全局的 IDF 的差异会随着索引里文档数的增多渐渐消失，在真实世界的数据量下，局部的 IDF 会被迅速均化，所以上述问题并不是相关度被破坏所导致的，而是由于数据太少。

​	解决：

​	1. 该索引只有1个分片

   2. ?search_type=dfs_query_then_fetch

      先分别获得每个分片本地的 IDF ，然后根据结果再计算整个索引的全局 IDF 。

      ```
      不要在生产环境上使用 dfs_query_then_fetch 。完全没有必要。只要有足够的数据就能保证词频是均匀分布的。没有理由给每个查询额外加上 DFS 这步。
      ```

### 11.6 【Doc Values】

当你对一个字段进行排序时，Elasticsearch 需要访问每个匹配到的文档得到相关的值。倒排索引的检索性能是非常快的，但是在字段值排序时却不是理想的结构，当排序的时候，我们需要倒排索引里面某个字段值的集合。如下倒排索引：

```text
Term      Doc_1   Doc_2   Doc_3
------------------------------------
brown   |   X   |   X   |
dog     |   X   |       |   X
dogs    |       |   X   |   X
fox     |   X   |       |   X
foxes   |       |   X   |
in      |       |   X   |
jumped  |   X   |       |   X
lazy    |   X   |   X   |
leap    |       |   X   |
over    |   X   |   X   |   X
quick   |   X   |   X   |   X
summer  |       |   X   |
the     |   X   |       |   X
------------------------------------
```

倒排索引是每个单词（Term）指向了文档的位置，获得所有包含 brown 的文档的词的完整列表：Doc_1和Doc_2是十分快速的。

Doc values 转置两者间的关系：每个文档包好了哪些term

这样检索使用倒排索引，排序使用Doc value

```text
Doc      Terms
-----------------------------------------------------------------
Doc_1 | brown, dog, fox, jumped, lazy, over, quick, the
Doc_2 | brown, dogs, foxes, in, lazy, leap, over, quick, summer
Doc_3 | dog, dogs, fox, jumped, over, quick, the
-----------------------------------------------------------------
```

**Doc Values 持久化**

​	Doc Values 是在索引时与 倒排索引 同时生成。也就是说 Doc Values 和 倒排索引 一样，基于 Segement 生成并且是不可变的。同时 Doc Values 和 倒排索引 一样序列化到磁盘，这样对性能和扩展性有很大帮助。

​	Doc Values 通过序列化把数据结构持久化到磁盘，我们可以充分利用操作系统的内存，而不是 JVM的 Heap 。 当 working set 远小于系统的可用内存，系统会自动将 Doc Values 驻留在内存中，使得其读写十分快速；不过，当其远大于可用内存时，系统会根据需要从磁盘读取 Doc Values，然后选择性放到分页缓存中。很显然，这样性能会比在内存中差很多，但是它的大小就不再局限于服务器的内存了。如果是使用 JVM 的 Heap 来实现那么只能是因为 OutOfMemory 导致程序崩溃了。

**Doc Values 数据压缩**

## 12. Ngrams

 即时搜索https://www.elastic.co/guide/cn/elasticsearch/guide/current/_ngrams_for_partial_matching.html

# document api

## 1. 索引API

`write.wait_for_active_shards`: 索引请求返回前需要等待多少个分片写入成功，默认是1，只要主分片写入成功就返回

`index.refresh_interval`  索引数据提交到刷新成新段的间隔，默认是一秒



## 2. get api

**实时性**

​	get API默认是实时的，不受索引刷新率的影响（当数据对搜索可见时）。如果文档已更新但尚未刷新，get API将就地发出刷新调用以使文档可见。自上次刷新以来，这还将使其他文档发生更改。

```console
禁用 realtime: false
```

**分散式**

​	get操作将散列到特定的分片中。然后将其重定向到该分片的副本之一，并返回结果。这意味着我们拥有的副本越多，我们将拥有越好的GET缩放比例。

​    `preference`

​             _local： 如果可能，该操作将首选在本地分配的分片上执行 

​             Custom(string) value : 确保相同的副本处理相同的自定义值       



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

