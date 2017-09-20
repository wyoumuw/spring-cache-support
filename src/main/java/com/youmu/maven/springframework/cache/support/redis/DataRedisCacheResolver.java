package com.youmu.maven.springframework.cache.support.redis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.youmu.maven.springframework.cache.Expireable;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public class DataRedisCacheResolver extends SimpleCacheResolver {

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null) {
            return Collections.emptyList();
        } else {
            Collection<Cache> result = new ArrayList<>();
            for (String cacheName : cacheNames) {
                // 如果是RedisCacheManager
                if (getCacheManager() instanceof RedisCacheManager
                        && context.getOperation() instanceof Expireable) {
                    Expireable expireable = (Expireable) context.getOperation();
                    // redis need unit second for expire
                    long expire = TimeUnit.SECONDS.convert(expireable.getExpire(),
                            expireable.getTimeUnit());
                    RedisCacheManager redisCacheManager = (RedisCacheManager) getCacheManager();
                    Field field = ReflectionUtils.findField(RedisCacheManager.class, "expires");
                    if (null == field) {
                        throw new UnsupportedOperationException("can not find redisCacheManager ");
                    }
                    ReflectionUtils.makeAccessible(field);
                    Map<String, Long> oldExpires = (Map<String, Long>) ReflectionUtils
                            .getField(field, redisCacheManager);
                    if (null == oldExpires) {
                        oldExpires = new ConcurrentHashMap<>();
                    }
                    oldExpires.put(cacheName, expire);
                    redisCacheManager.setExpires(oldExpires);
                }
                Cache cache = getCacheManager().getCache(cacheName);
                if (cache == null) {
                    throw new IllegalArgumentException("Cannot find cache named '" + cacheName
                            + "' for " + context.getOperation());
                }
                result.add(cache);
            }

            return result;
        }
    }
}
