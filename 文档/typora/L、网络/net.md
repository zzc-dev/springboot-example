## 1、网络模型

### 1.1、OSI七层模型

1、物理层：提供传输信号功能，载体为物理设备（光纤、集线器、调制解调器等），协议有（IEEE802.2等）；
2、链路层：提供可靠的数据传输功能，使用链路地址（mac地址）来访问介质，协议有（802.11、HDLC、STP、ARP等）；
3、网络层：提供IP选址及路由选择功能，包括（网络寻径、流量控制、错误检查等），协议有（IP、ICMP、IGMP、RIP等）；
4、传输层：提供网络数据流传输功能，协议有（TCP、UDP等）；
5、会话层：提供建立、管理和终止表示层与实体之间的通信会话功能，协议有（TLS、SSL等）；
6、表示层：提供用于应用层数据的编码和转换功能，如数据压缩和加密；
7、应用层：提供为各种面向用户的应用软件接入网络服务的功能，协议有（HTTP、FTP、SMTP、POP3、SSH、TELNET等）；

### 1.2、TCP/IP四层模型

1、网络接口层：提供地址解析协议（ARP、RARP）等，对应链路层；
2、网间层：提供IP协议、RIP协议（路由信息协议）等，负责数据的包装、寻址及路由，对应网络层；
3、传输层：提供TCP协议、UDP协议等，对应传输层；
4、应用层：提供HTTP协议、FTP协议、Telent协议、SMTP协议等，对应应用层及表示层；

## 2、TCP/UDP协议

### 2.1、TCP协议

1、特点：可靠的、面向连接的字节流服务（全双工）；
2、结构：首部（20字节：源端口、目的端口、序号seq、确认号ack、标志位[ACK、SYN、FIN等]、窗口大小、检验和等）、数据；
3、三次握手：（SYN=1、seq=x） => （SYN=1、ACK=1、seq=y、ack=+1） => (ACK=1、seq=x+1、ack=y+1)；
4、四次挥手：（FIN=1、seq=u） => (ACK=1、seq=v、ack=u+1) => (FIN=1、ACK=1、seq=w、ack=u+1) => (ACK=1、seq=u+1、ack=w+1)；
5、拥塞控制：慢启动（开始时，拥塞窗口为1，可以按指数级递增）、拥塞避免（拥塞窗口到达限定值，线性加1）、快重传（发送方尽快进行重传，而不是等超时重传计时器超时再重传）、快恢复（丢失个别的报文时，不启动慢开始算法，而执行快恢复算法）；
6、流量控制：通过滑动窗口的大小改变来实现，由接收方（可动态调整）回给发送方，通过缓冲区提高效率；
7、可靠传输：数据分段（合理长度）、超时重发、确认响应、数据校验（包有问题则丢弃，等发送端超时重发）、重排序、丢弃重复数据、流量控制；

### 2.2、UDP协议

1、特点：不可靠、无连接、快；
2、面向数据包：没有TCP的分组开销；

### 2.3、TCP与UDP的区别

​				TCP								UDP
是否连接			连接								非连接
传输可靠			可靠								不可靠
传输内容			字节流（分割成组，接收端重组）		报文（没有分组开销）
拥塞/流量控制		提供								不提供
首部大小			20字节							8字节
一对多			一对一							一对一、一对多、多对一、多对多

### 2.4、TCP粘包

1、概念：默认情况下，tcp连接会启用延时传输算法（Nagle算法），在数据发送之前缓存他们，短时间内有多个数据会缓存到一起做一次发送，这样可以减少IO的消耗提升性能；
2、解决：多次发送之间留间隔、关闭Nagle算法、拆包/封包（包与包之间插入一些特征数据后进行分割）；
3、UDP会吗：TCP是面向流的协议，UDP是面向消息的协议，含有消息保护边界，不能一次提取任意字节的消息；

## 3、其他协议

### 3.1、DNS协议

1、概念：将IP和域名相互映射的分布式数据库，方便使用；
2、左右：将域名解析为IP地址；
3、存储过程：跟DNS服务器、顶级DNS服务器（.com、.org等）、权威DNS服务器（二级域名）；
4、解析过程：浏览器输入域名进行访问 => 检查hosts文件是否有地址映射（存在即返回） => 检查本地DNS解析器缓存（存在即返回） => 向首选DNS服务器（本地配置）发起请求（默认缓存3天） => 如果首选DNS服务器不是权威DNS服务器则需向权威DNS服务器转发改请求 => 向根DNS服务器发起请求（根DNS服务器下存储了一级域.com、.org等，返回一级域的IP给首选DNS服务器） => 向一级域DNS服务器发起请求（一级DNS服务器下存储了二级域信息） => 向二级域DNS服务器发起请求.......返回IP给首选服务器 => 返回IP给PC => PC向IP地址发起请求（客户端到本地DNS服务器属于递归查询，DNS服务器之间的交互属于迭代查询）；
5、优化策略：尽可能少的域名解析、浏览器的DNS预解析（dns-prefetch）；

### 3.2、IP协议

1、特点：不可靠（直接丢弃并通知源）、无连接（无状态）；
2、结构：首部（20字节：版本号、首部长度、服务类型、总长度、标识、源IP、目的IP、选项等）、数据；

## 4、HTTP协议

### 4.1、报文内容

1、请求报文：请求行（method、url、协议版本，空格分隔）、请求头、空行、请求体；
2、响应报文：响应行（协议版本、状态码、状态码描述）、响应头、空行、响应体；

### 4.2、版本区别

#### 4.2.1、http1.1、http1.0区别

1、缓存处理（缓存头，新增Cache-Control、ETag、If-Match、If-None-Match、If-Unmodified-Since等）；
2、带宽优化及网络连接的使用（支持range头即断点续传，支持不发送请求/响应body部分）；
3、错误通知的管理（新增29个错误管理状态码，如：409冲突、410被删除等）；
4、host头处理（请求必须带host头域，否则报错400）；
5、长连接（keep-alive，后端配置connection头控制）、pipeline（流水线功能）；
6、新增五种方法：options、put、delete、trace、connect；

#### 4.2.2、http2.0、http1.1区别

1、多路复用（一个连接并发处理多个请求）；
2、二进制传输（帧：二进制分帧，通信的最小单位，消息由一个或多个帧组成；流：存在于连接中的虚拟通道，可以承载双向消息，每个流都有唯一的整数ID）；
3、头部压缩（使用hpack算法）；
4、服务端推送（server push）；

### 4.3、头字段

#### 4.3.1、客户端描述

1、accept：客户端能够接收的内容类型，如text/plain、text/html等；
2、accept-charset：客户端能够接受的字符编码集，如果utf-8等；
3、accept-encoding：客户端能够接受的压缩编码类型，如果gzip、compress等；
4、accept-language：客户端能够接受的语言，如果en、zh等；
5、connection：是否需要持久连接；
6、referer：请求网页的地址；
7、user-agent：客户端信息；

#### 4.3.2、请求的描述

1、cookie：缓存信息；
2、content-length：内容长度；
3、content-type：内容类型；
4、date：请求发送的日期；
5、host：请求的服务器域名及端口号；
6、upgrade：升级/转换协议；

#### 4.3.3、缓存的描述

1、expires：缓存控制；
2、cache-control：缓存控制；
3、etag：资源标识；
4、if-none-match：对etag的匹配；
5、last-modified：最后修改时间；
6、if-modified-since：对last-modified的匹配；

#### 4.3.4、跨域的描述

1、access-control-allow-origin：允许源跨域；
2、access-control-allow-method：允许方法跨域；
3、access-control-allow-headers：允许自定义头跨域；
4、access-control-allow-credentials：允许携带cookie跨域；
5、access-control-max-age：允许规定时间内不再重复验证跨域；
6、access-control-expose-headers：允许对客户端暴露非简单、自定义头部；

### 4.4、方法

方法		有请求体		有响应体		是否安全		是否幂等		可缓存	     表单支持		描述
get			否			是			是			是		    是		是
head		否			否			是			是		    是		否			文件长度
post			是			是			否			否		    基本否		是
put			是			是			否			是		    否		否			201/204
delete		是			是			否			是		    否		否			204/404问题
connect		否			是			否			否		    否		否
options		否			是			是			是		    否		否			跨域问题
trace		否			否			否			是		    否		否
patch		是			否			否			否		    否		否

### 4.5、状态码

#### 4.5.1、100系列

100：continue，继续；
101：switch protocols，切换协议；
102：processing，正在处理；

#### 4.5.2、200系列

200：ok，成功；
201：created，服务器接受并且创建了资源（put）；
202：accept，服务器已接受；
204：no-content，无内容，例如head请求；
206：partial-content，分片上传/下载使用，请求头必须包含if-range来作为请求条件，响应头必须包含content-range、date、etag或者content-location等；

#### 4.5.3、300系列

300：multiple choices，多种选择；
301：moved permanently，永久重定向，仅限get、head请求；
302：moved temporarily，临时重定向；
304：not-modified，无修改，常用于浏览器协商缓存；

#### 4.5.4、400系列

400：bad request，语义错误或参数错误；
401：unauthorized，未认证；
403：forbidden，未授权；
404：not found；
405：method not allowed；
411：length-required，必须带长度字段；
413：request entity too large，请求体太大；
415：unsupported mediatype，不支持的媒体类型；

#### 4.5.5、500系列

500：internal server error，服务器内部错误；
502：bad gateway，代理或上行服务有问题；
503：service unavailable，服务器过载或不正常（不可用）；
504：gateway timeout，代理超时；
505：httpversion not supported，http版本不支持；

#### 4.5.6、302/303/307区别

1、302是http1.0的协议状态码，http1.1为了细化又加了303、307；
2、303表示客户端应该用get获取资源，会把post请求变为get请求进行重定向；
3、307会遵照浏览器标准，不会从post转为get；

### 4.6、HTTPS协议

1、验证身份：证书（包含：公钥、域名或IP、组、加密算法、有效期、颁发者、指纹、数字签名）；
2、内容加密：ssl/tls非对称加密认证，之后使用对称秘钥加密解密；
3、防止篡改：消息原文（部分关键信息）提前摘要后使用ca私钥对其进行加密，即为数字签名，浏览器使用ca公钥去解密证书签名来验证是否被篡改；
4、默认使用443端口，可以防止运营商劫持；

### 4.7、WebSocket协议

1、兼容性：IE10、FF6、Chrome14、Safiri6等；
2、降级兼容：sockjs、socket.io，xhr-streaming（持久连接+分块传输编码，只允许服务器向客户的单向推数据）、eventsource（不支持IE）、long-poll（长轮询，服务器不关闭连接，客户端主动进行超时关闭重连）；
3、协议原理：基于TCP协议（帧形式数据）、具有命名空间、可以和http server共享同一端口；
4、请求头：connection（upgrade+201）、upgrade（websocket）、sec-websocket-key（用于验证的标识）、sec-websocket-version（确认版本）、sec-websocket-extensions（支持的插件）；
5、响应头：connection（upgrade）、upgrade（websocket）、sec-websocket-accept（接受的标识，从前端的key中加上固定标识sha1得到结果）；

### 4.8、缓存

#### 4.8.1、强缓存

1、pragma：http1.0，优先级高于expires，已废弃；
2、expires：http1.0，使用绝对日期，单位秒；
3、cache-control：http1.1，常用值（private-仅客户端、public-客户端及代理服务器、max-age-相对时间秒、no-cache-协商缓存、no-store-不缓存）；
4、处理策略：命中则直接使用，过期删除；

#### 4.8.2、协商缓存

1、last-modified：最后修改时间；
2、if-modified-since：对应last-modified，一致则304，不一致则200；
3、etag：资源唯一标识，通过计算资源长度+最后修改时间计算，优先级高于last-modified；
4、if-none-match：对应etag，一致则304，不一致则200；
5、处理策略：命中时判断有效期，过期则跟服务器进行验证，并设置下次过期时间；

#### 4.8.3、默认缓存

1、expires：响应头没有提供其他有效缓存策略时，expires=（Date.now - lastModified）* 0.1，如果时间匹配，则使用缓存，否则进行验证；

### 4.9、跨域

1、cors：cross origin resource sharing，跨域资源共享；
2、简单请求：包括简单请求方法（get、head、post）、请求头（可设置：accept、accept-language、content-language、content-type、DPR、width、viewport-width）、请求文档类型（text/plain、multipart/form-data、application/x-www-form-urlencoded）、xhr.upload（send数据时，upload对象没有绑定任何监听事件）、请求中没有使用ReadableStream对象（post、put均可发送可读流对象）；
3、预检请求：xhr或fetch属于非简单请求时，浏览器会发送一个options预检请求，并设置其头部access-control-allow-origin/methods/headers等，正式请求也必须附带此响应头才可以；
4、解决方案：cors、jsonp、代理、postMessage；