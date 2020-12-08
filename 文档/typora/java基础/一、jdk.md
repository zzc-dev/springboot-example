# 一、String.intern

>jdk1.8中，尝试将该字符串放入字符串常量池，
>		如果该常量池没有，则放入并返回该字符串对象
>		否则返回常量池中的对象

eg:

```
String s1 = new String("11"); //将11放入常量池并返回字符串对象，创建了两个对象
boolean f = s1.intern()==s1; // false 常量池 != 字符串对象

String s2 = new String("a") + new String("b"); // a和b放入了字符串常量池，并返回ab字符串对象
boolean f2 = s2.intern()==s2; // true 常量池中没有ab，在常量池中加入ab并返回字符串对象 

String s3 = new String("ja") + new String("va");
boolean f3 = s3.intern()==s3; // false java关键池存在常量池中返回常量池的对象
```

# 二、HashMap

## 1. 容量为什么要是2的倍数

​     hashmap的key计算数组的下标，index = hash & length-1，与运算得到下标返回是0-length-1

​     &运算比%效率更高

## 2. 计算hash时为什么需要与高位异或运算

由index = hash & length-1可知，index只与hash的低位有关，当hash低位相同时，高位无论是什么，index都相同，这样会增大哈希碰撞的概率。低位与高位异或使得hash的更多的位数参与到获取index的运算中，最终是的hash散列分布

## 3. jdk1.7头插法死循环问题

```
void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
　　　　　//for循环中的代码，逐个遍历链表，重新计算索引位置，将老数组数据复制到新数组中去
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);//根据新数组的大小  下标计算出重新存入的　　位置。
　　　　　　　　　 //将当前entry的next链指向新的索引位置,newTable[i]有可能为空，有可能也是个entry链，如果是entry链，直接在链表头部插入。
                e.next = newTable[i];
                newTable[i] = e;
                e = next;//链表向后
            }
        }
    }
```

```
1. Entry<K,V> next = e.next; //线程1执行完阻塞 e=1 e.next=3
```

![image-20201207224227145](D:\myself\springboot-example\文档\typora\images\map.png)

       ```
2. 线程2 resize过程
       ```

<img src="D:\myself\springboot-example\文档\typora\images\map01.png" alt="image-20201207224614722" style="zoom:80%;" />

```
3. 线程2扩容完成后线程1继续执行，执行到e.next = newTable[i];
   1.next = newTable[i] = 3  => 1->3
   之后get(3)将死循环
```

![在这里插入图片描述](D:\myself\springboot-example\文档\typora\images\map03.png)

产生循环链表后带来的问题是什么?

环形链表已经产生了, 当我们调用get(3)或者get(1)不会产生问题,
但是如果get(5), 并且5在数组中的下标和1,3的一致的话, 由于链表中没有5, 所以就会一直在链表中寻找, 但是链表没有尽头, 就导致程序卡在get(5)处了

## 4.  jdk1.7和jdk1.8

jkd1.7 数组+链表 ：采用头插法

   随着数据的增多，单个节点上的链表长度会越来越大，查询效率逐渐降低。

   resize容量翻倍，需要重新计算index，index = index  or index + 扩容前的容量

```
                         
         hash=0000 0111  length=16 length-1=0000 1111 
hash & length -1    0000 0111 = 7
                 resize: length=32 length-1=0001 1111
hash & length -1    0000 0111 = 7 这种情况下不变，但如果hash为xxx1 0111，index = 16+7=23                 
```



jdk1.8 数组+链表+红黑树：采用尾插法

​    采用红黑树的原因：红黑树插入和查找速度都可以（略慢于avl树，他是不严格的avl树），但删除效率远高于avl树。（avl树删除后，该节点及其祖先节点可能都需要重平衡，而红黑树最多三次旋转操作即可重新达到平衡）

​    使用尾插法的原因：本身链表和红黑树的转换就需要遍历，其次头插法存在死循环问题

```
DEFAULT_INITIAL_CAPACITY=16; // 默认初始容量
DEFAULT_LOAD_FACTOR=0.75f; // 扩容阈值
TREEIFY_THRESHOLD=8; // 链表的最大长度,当超过该长度时该节点变成红黑树
UNTREEIFY_THRESHOLD=6; // 单个节点红黑树的最小长度，当小于该长度时该节点变成链表
```

# 三、 LinkedHashMap、TreeMap

HashMap和Hashtable不保证数据有序，LinkedHashMap保证数据可以保持插入顺序，而TreeMap可以按key的大小顺序排序

 LinkedHashMap在HashMap的基础上多了一个双向链表来维持顺序

# 四、迭代器

## 1. modCount和fail-fast

modCount代表对集合的操作次数
modCount既不能确保**可见性（**[volatile](https://www.cnblogs.com/chengxiao/p/6528109.html)），modCount++；又不是原子操作。

在迭代遍历线程不安全的集合的时候，如ArrayList，如果其他线程修改了该集合，那么将抛出ConcurrentModificationException，这就是 fail-fast 策略

fail-fast机制，是一种错误检测机制。它只能被用来检测错误，因为JDK并不保证fail-fast机制一定会发生。

![image-20201207230526432](D:\myself\springboot-example\文档\typora\images\itr01.png)



## 2. 为什么要使用iterator

# 五、ConcurrentHashMap

