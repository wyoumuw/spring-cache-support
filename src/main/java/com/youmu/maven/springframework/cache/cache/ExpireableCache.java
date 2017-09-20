package com.youmu.maven.springframework.cache.cache;

import org.springframework.cache.Cache;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public interface ExpireableCache extends Cache {
    public void put(Object key, Object value, long expire);

    public ValueWrapper putIfAbsent(Object key, Object value, long expire);
}
