package com.zzc.springbootjpa.jpql.scroll;

import java.io.Closeable;

/**
 * 流式接口，用于处理大数据量的结果集
 */
public interface ScrollResult extends Closeable {
    /**
     * 检查是否有数据
     * @return
     */
    boolean next();

    /**
     * 获取数据，会根据resultClass进行相应转化
     * @return
     */
    Object get();

    /**
     * 获取原始对象，如果对于native的类型，返回时map, 否则返回tuple
     * @return
     */
    Object getOriginal();
}
