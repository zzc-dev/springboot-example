# 一、Queue

api   https://blog.csdn.net/a724888/article/details/80275501

```
add/offer      队尾添加          超过队列长度：抛异常/返回false
remove/poll    获取对头数据并移除  队列无数据：抛异常/返回null
element/peek   获取对头数据       队列无数据：抛异常/返回null
```

# 二、Deque

> DeQueue(Double-ended queue)为接口，继承了Queue接口，创建双向队列，灵活性更强，可以前向或后向迭代，在队头队尾均可心插入或删除元素。它的两个主要实现类是ArrayDeque和LinkedList。