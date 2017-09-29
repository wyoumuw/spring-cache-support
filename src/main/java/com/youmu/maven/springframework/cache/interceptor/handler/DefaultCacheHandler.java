package com.youmu.maven.springframework.cache.interceptor.handler;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperation;

import java.util.concurrent.Callable;

/**
 * @Author: YLBG-LDH-1506
 * @Description:
 * @Date: 2017/09/29
 */
public class DefaultCacheHandler implements CacheHandler {
    @Override
    public Object getNativeCache(Cache cache, CacheOperation cacheOperation) {
        return cache.getNativeCache();
    }

    @Override
    public Cache.ValueWrapper get(Object key, Cache cache, CacheOperation cacheOperation) {
        return cache.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type, Cache cache, CacheOperation cacheOperation) {
        return cache.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader, Cache cache,
            CacheOperation cacheOperation) {
        return cache.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value, Cache cache, CacheOperation cacheOperation) {
        cache.put(key, value);
    }

    @Override
    public Cache.ValueWrapper putIfAbsent(Object key, Object value, Cache cache,
            CacheOperation cacheOperation) {
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key, Cache cache, CacheOperation cacheOperation) {
        cache.evict(key);
    }

    @Override
    public void clear(Cache cache, CacheOperation cacheOperation) {
        cache.clear();
    }
}
