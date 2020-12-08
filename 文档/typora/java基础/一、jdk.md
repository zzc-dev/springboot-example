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

## 5.1 jdk1.7

jdk1.7中，chm使用分段锁技术，将数据分成一段一段存储，并且对每段数据加锁，当一个线程访问其中一个段的数据时，另外的线程访问其他的分段不受任何影响，在实现并发访问的同时保证了效率。

ConcurrentHashMap中主要实体类就是三个：ConcurrentHashMap（整个Hash表）,Segment（桶），HashEntry（节点）

​	     static final class Segment<K,V> extends ReentrantLock implements Serializable

不变(Immutable)和易变(Volatile)ConcurrentHashMap完全允许多个读操作并发进行，读操作并不需要加锁

```
static final class HashEntry<K,V> {
    final int hash;
    final K key;
    volatile V value;
    volatile HashEntry<K,V> next;
}    
```

### 5.1.1 初始化

initialCapacity: 初始容量 默认16
loadFactor： 负载因子 默认0.75f
concurrencyLevel： 并发级别 默认16 用于确认Segment数组的容量，也就是允许同时操作的最大线程数

1. 验证参数的合法性
2. 并发级别不能超过MAX_SEGMENTS 
3. 根据concurrenyLevel计算分段数，分段数ssize总是2的n次幂，计算segmentShift、segmentMask
4. 计算每个Segment平均应该放置多少个元素
5. 最后创建一个Segment实例，将其当做Segment数组的第一个元素

```
public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;
        // Find power-of-two sizes best matching arguments
        int sshift = 0;   //记录sszie向左移位的次数
        int ssize = 1;    // 数组的大小
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        this.segmentShift = 32 - sshift; 
        this.segmentMask = ssize - 1;  // 用于计算hash  2^n-1 低位总是1
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        int c = initialCapacity / ssize;  // 每个Segment的容量
        if (c * ssize < initialCapacity)
            ++c;
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        while (cap < c)
            cap <<= 1;
        // create segments and segments[0]
        Segment<K,V> s0 =
            new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
                             (HashEntry<K,V>[])new HashEntry[cap]);
        Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
        UNSAFE.putOrderedObject(ss, SBASE, s0); // ordered write of segments[0]
        this.segments = ss;
    }
```

### 5.1.2 put

1. 检查key值，不允许为null

2. 根据key得到hash，hash(key)将key.hashCode的高位和低位都参与了运算，让hash散列分布

3. 用2的得到的hash右移segmentShift位后与segmentMask与运算，根据hash的高位得到Segment数组的索引

4. 使用Unsafe的方式从Segment数组中获取该索引对应的Segment对象

5. put中使用lock锁住整个Segment，然后根据hash= (tab.length - 1) & hash 算出该key在Segment中的位置

   然后使用头插法插入数据

```
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null)
        throw new NullPointerException();
    int hash = hash(key);
    int j = (hash >>> segmentShift) & segmentMask;
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
         (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        s = ensureSegment(j);
    return s.put(key, hash, value, false);
}
```

### 5.1.3 get

1. 和put操作一样，先通过key进行两次hash确定应该去哪个Segment中取数据。
2. 使用Unsafe获取对应的Segment，然后再进行一次&运算得到HashEntry链表的位置，然后从链表头开始遍历整个链表（因为Hash可能会有碰撞，所以用一个链表保存），如果找到对应的key，则返回对应的value值，如果链表遍历完都没有找到对应的key，则说明Map中不包含该key，返回null。

值得注意的是，get操作是不需要加锁的（如果value为null，会调用readValueUnderLock，只有这个步骤会加锁），通过前面提到的volatile和final来确保数据安全。

### 5.1.4 size、containsValue操作

size操作与put和get操作最大的区别在于，size操作需要遍历所有的Segment才能算出整个Map的大小，而put和get都只关心一个Segment。假设我们当前遍历的Segment为SA，那么在遍历SA过程中其他的Segment比如SB可能会被修改，于是这一次运算出来的size值可能并不是Map当前的真正大小。所以一个比较简单的办法就是计算Map大小的时候所有的Segment都Lock住，不能更新(包含put，remove等等)数据，计算完之后再Unlock。这是普通人能够想到的方案，但是牛逼的作者还有一个更好的Idea：先给3次机会，不lock所有的Segment，遍历所有Segment，累加各个Segment的大小得到整个Map的大小，如果某相邻的两次计算获取的所有Segment的更新的次数（每个Segment都有一个modCount变量，这个变量在Segment中的Entry被修改时会加一，通过这个值可以得到每个Segment的更新操作的次数）是一样的，说明计算过程中没有更新操作，则直接返回这个值。如果这三次不加锁的计算过程中Map的更新次数有变化，则之后的计算先对所有的Segment加锁，再遍历所有Segment计算Map大小

## 5.2 jdk1.8

## 5.3 区别

在 JDK1.7 中，ConcurrentHashMap 采用了分段锁策略，将一个 HashMap 切割成 Segment 数组，其中 Segment 可以看成一个 HashMap， 不同点是 Segment 继承自 ReentrantLock，在操作的时候给 Segment 赋予了一个对象锁，从而保证多线程环境下并发操作安全。

但是 JDK1.7 中，**HashMap 容易因为冲突链表过长，造成查询效率低**，所以在 JDK1.8 中，HashMap 引入了红黑树特性，当冲突链表长度大于 8 时，会将链表转化成红黑二叉树结构。

在 JDK1.8 中，与此对应的 ConcurrentHashMap 也是采用了与 HashMap 类似的存储结构，但是 JDK1.8 中 ConcurrentHashMap 并没有采用分段锁的策略，而是在元素的节点上采用 `CAS + synchronized` 操作来保证并发的安全性，源码的实现比 JDK1.7 要复杂的多。