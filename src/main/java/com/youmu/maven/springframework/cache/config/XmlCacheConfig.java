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
 * @Author: YLBG-LDH-1506
 * @Description:
 * @Date: 2017/09/21
 */
@Configuration
public class XmlCacheConfig {
    @Autowired(required = false)
    private CustomableCacheAnnotationParser cacheAnnotationParser;

    @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryCacheOperationSourceAdvisor cacheAdvisor() {
        BeanFactoryCacheOperationSourceAdvisor advisor = new BeanFactoryCacheOperationSourceAdvisor();
        advisor.setCacheOperationSource(cacheOperationSource());
        advisor.setAdvice(cacheInterceptor());
        advisor.setOrder(Ordered.LOWEST_PRECEDENCE);
        return advisor;
    }

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

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired(required = false)
    private CacheResolver cacheResolver;

    @Autowired(required = false)
    private KeyGenerator keyGenerator;

    @Autowired(required = false)
    private CacheErrorHandler errorHandler;

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
}
