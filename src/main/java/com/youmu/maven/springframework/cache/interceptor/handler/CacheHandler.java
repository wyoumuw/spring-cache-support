package com.youmu.maven.springframework.cache.interceptor.handler;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperation;

import java.util.concurrent.Callable;

/**
 * @Author: YLBG-LDH-1506
 * @Description:
 * @Date: 2017/09/29
 */
public interface CacheHandler {
    public Object getNativeCache(Cache cache, CacheOperation cacheOperation);

    public Cache.ValueWrapper get(Object key, Cache cache, CacheOperation cacheOperation);

    public <T> T get(Object key, Class<T> type, Cache cache, CacheOperation cacheOperation);

    public <T> T get(Object key, Callable<T> valueLoader, Cache cache,
            CacheOperation cacheOperation);

    public void put(Object key, Object value, Cache cache, CacheOperation cacheOperation);

    public Cache.ValueWrapper putIfAbsent(Object key, Object value, Cache cache,
            CacheOperation cacheOperation);

    public void evict(Object key, Cache cache, CacheOperation cacheOperation);

    public void clear(Cache cache, CacheOperation cacheOperation);
}
