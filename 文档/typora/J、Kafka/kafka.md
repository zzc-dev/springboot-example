一、简介

Kafka 是linkedin 公司用于日志处理的分布式消息队列，同时支持离线和在线日志处理。kafka 对消息保存时根据Topic进行归类，发送消息者成为Producer,消息接受者成为Consumer,此外kafka 集群有多个kafka 实例组成，每个实例(server)称为broker。无论是kafka集群，还是producer和consumer 都依赖于zookeeper 来保证系统可用性，为集群保存一些meta 信息。

kafka基于分布式的发布订阅的消息队列

消费者端主动拉取数据

解耦，消峰，解决生产者速度>消费者速度的问题