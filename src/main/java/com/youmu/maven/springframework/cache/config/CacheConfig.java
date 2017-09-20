package com.youmu.maven.springframework.cache.config;

import com.youmu.maven.springframework.cache.interceptor.CustomableCacheInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;

import com.youmu.maven.springframework.cache.parser.CustomableCacheAnnotationParser;
import com.youmu.maven.springframework.cache.parser.ExpireableCacheAnnotationParser;

@EnableCaching
@Configuration
@Order
public class CacheConfig implements CachingConfigurer {

    @Autowired(required = false)
    CustomableCacheAnnotationParser cacheAnnotationParser;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
        return new AnnotationCacheOperationSource((null == cacheAnnotationParser)
                ? new ExpireableCacheAnnotationParser() : cacheAnnotationParser);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CustomableCacheInterceptor cacheInterceptor() {
        CustomableCacheInterceptor interceptor = new CustomableCacheInterceptor();
        interceptor.setCacheOperationSources(cacheOperationSource());
        if (this.cacheResolver != null) {
            interceptor.setCacheResolver(this.cacheResolver);
        } else if (this.cacheManager != null) {
            interceptor.setCacheManager(this.cacheManager);
        }
        if (this.keyGenerator != null) {
            interceptor.setKeyGenerator(this.keyGenerator);
        }
        if (this.errorHandler != null) {
            interceptor.setErrorHandler(this.errorHandler);
        }
        return interceptor;
    }

    private CacheManager cacheManager;

    private CacheResolver cacheResolver;

    private KeyGenerator keyGenerator;

    private CacheErrorHandler errorHandler;

    @Override
    public CacheManager cacheManager() {
        return cacheManager;
    }

    @Override
    public CacheResolver cacheResolver() {
        return cacheResolver;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return keyGenerator;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return errorHandler;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheResolver(CacheResolver cacheResolver) {
        this.cacheResolver = cacheResolver;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void setErrorHandler(CacheErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    // @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    // @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    // public BeanFactoryCacheOperationSourceAdvisor cacheAdvisor(CacheManager
    // cacheManager) {
    // BeanFactoryCacheOperationSourceAdvisor advisor =
    // new BeanFactoryCacheOperationSourceAdvisor();
    // advisor.setCacheOperationSource(cacheOperationSource());
    // advisor.setAdvice(cacheInterceptor(cacheManager));
    // return advisor;
    // }
    //
    //
    // @Bean
    // @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    // public CustomableCacheAspectSupport cacheInterceptor(CacheManager cacheManager) {
    // CustomableCacheAspectSupport interceptor = new CustomableCacheAspectSupport();
    // interceptor.setCacheOperationSources(cacheOperationSource());
    // interceptor.setCacheManager(cacheManager);
    // return interceptor;
    // }
}
