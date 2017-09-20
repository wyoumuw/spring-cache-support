package com.youmu.maven.springframework.cache.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/20
 */
public class SimpleCacheConfig implements CachingConfigurer {
    private CacheManager cacheManager;

    private CacheResolver cacheResolver;

    private KeyGenerator keyGenerator;

    private CacheErrorHandler errorHandler;

    @Override
    public CacheManager cacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public CacheResolver cacheResolver() {
        return cacheResolver;
    }

    public void setCacheResolver(CacheResolver cacheResolver) {
        this.cacheResolver = cacheResolver;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(CacheErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
