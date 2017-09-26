package com.youmu.maven.springframework.cache.support.redis;

import com.youmu.maven.springframework.cache.config.EnableCustomableCache;
import com.youmu.maven.springframework.cache.config.XmlCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

/**
 * @Author: YLBG-LDH-1506
 * @Description:
 * @Date: 2017/09/21
 */
@Configuration
@EnableCustomableCache
public class SimpleRedisCacheConfig implements CachingConfigurer {

    @Autowired
    RedisCacheManager redisCacheManager;
    @Autowired(required = false)
    KeyGenerator keyGenerator;
    @Autowired(required = false)
    CacheErrorHandler cacheErrorHandler;

    @Bean
    public DataRedisCacheResolver redisCacheResolver() {
        DataRedisCacheResolver redisCacheResolver = new DataRedisCacheResolver();
        redisCacheResolver.setCacheManager(redisCacheManager);
        return redisCacheResolver;
    }

    @Override
    public CacheManager cacheManager() {
        return redisCacheManager;
    }

    @Override
    public CacheResolver cacheResolver() {
        return redisCacheResolver();
    }

    @Override
    public KeyGenerator keyGenerator() {
        return keyGenerator;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return cacheErrorHandler;
    }
}
