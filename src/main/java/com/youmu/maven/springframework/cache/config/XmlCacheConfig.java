package com.youmu.maven.springframework.cache.config;

import com.youmu.maven.springframework.cache.interceptor.CustomableCacheInterceptor;
import com.youmu.maven.springframework.cache.parser.CustomableCacheAnnotationParser;
import com.youmu.maven.springframework.cache.parser.ExpireableCacheAnnotationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/21
 */
@Configuration
@EnableCustomableCache
public class XmlCacheConfig implements CachingConfigurer {
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