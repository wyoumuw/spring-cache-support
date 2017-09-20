package com.youmu.maven.springframework.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;

import com.youmu.maven.springframework.cache.interceptor.CustomableCacheInterceptor;
import com.youmu.maven.springframework.cache.parser.CustomableCacheAnnotationParser;
import com.youmu.maven.springframework.cache.parser.ExpireableCacheAnnotationParser;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
@Order
public class CacheConfig extends ProxyCachingConfiguration {

    @Autowired(required = false)
    private CustomableCacheAnnotationParser cacheAnnotationParser;

    @Override
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
        return new AnnotationCacheOperationSource((null == cacheAnnotationParser)
                ? new ExpireableCacheAnnotationParser() : cacheAnnotationParser);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Override
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

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableCaching = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCaching.class.getName(), false));
        if (null == this.enableCaching
                && null == (this.enableCaching = AnnotationAttributes.fromMap(importMetadata
                        .getAnnotationAttributes(EnableCaching.class.getName(), false)))) {
            this.enableCaching = getDefaultEnableCachingAttr();
            // throw new IllegalArgumentException(
            // "@EnableCaching is not present on importing class " +
            // importMetadata.getClassName());
        }
    }

    private AnnotationAttributes getDefaultEnableCachingAttr() {
        AnnotationAttributes attr = new AnnotationAttributes();
        attr.put("proxyTargetClass", false);
        attr.put("mode", AdviceMode.PROXY);
        attr.put("order", Ordered.LOWEST_PRECEDENCE);
        return attr;
    }
}
