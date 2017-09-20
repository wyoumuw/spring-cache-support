package com.youmu.maven.springframework.cache.parser;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.youmu.maven.springframework.cache.ExpireableCacheableOperation;
import com.youmu.maven.springframework.cache.annotation.ExpireableCachePut;
import com.youmu.maven.springframework.cache.annotation.ExpireableCacheable;
import com.youmu.maven.springframework.cache.annotation.ExpireableCaching;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/18
 */
public class ExpireableCacheAnnotationParser extends CustomableCacheAnnotationParser {
    @Override
    protected Collection<CacheOperation> customCache(Collection<CacheOperation> ops,
            DefaultCacheConfig cachingConfig, AnnotatedElement ae) {
        Collection<CacheOperation> operations = ops;

        Collection<ExpireableCacheable> expireableCacheables = AnnotatedElementUtils
                .getAllMergedAnnotations(ae, ExpireableCacheable.class);
        if (!expireableCacheables.isEmpty()) {
            operations = lazyInit(operations);
            for (ExpireableCacheable expireableCacheable : expireableCacheables) {
                operations.add(
                        parseExpireableCacheableAnnotation(ae, cachingConfig, expireableCacheable));
            }
        }
        Collection<ExpireableCachePut> puts = AnnotatedElementUtils.getAllMergedAnnotations(ae,
                ExpireableCachePut.class);
        if (!puts.isEmpty()) {
            operations = lazyInit(operations);
            for (ExpireableCachePut put : puts) {
                operations.add(parseExpireableCacheablePutAnnotation(ae, cachingConfig, put));
            }
        }
        Collection<ExpireableCaching> expireableCachings = AnnotatedElementUtils
                .getAllMergedAnnotations(ae, ExpireableCaching.class);
        if (!expireableCachings.isEmpty()) {
            operations = lazyInit(operations);
            for (ExpireableCaching expireableCaching : expireableCachings) {
                Collection<CacheOperation> cachingOperations = parseExpireableCachingAnnotation(ae,
                        cachingConfig, expireableCaching);
                if (cachingOperations != null) {
                    operations.addAll(cachingOperations);
                }
            }
        }
        return operations;
    }

    protected Collection<CacheOperation> parseExpireableCachingAnnotation(AnnotatedElement ae,
            DefaultCacheConfig defaultConfig, ExpireableCaching expireableCaching) {
        Collection<CacheOperation> ops = null;
        ExpireableCacheable[] expireableCacheables = expireableCaching.cacheable();
        if (!ObjectUtils.isEmpty(expireableCacheables)) {
            ops = lazyInit(ops);
            for (ExpireableCacheable expireableCacheable : expireableCacheables) {
                ops.add(parseExpireableCacheableAnnotation(ae, defaultConfig, expireableCacheable));
            }
        }
        ExpireableCachePut[] expireableCachePuts = expireableCaching.put();
        if (!ObjectUtils.isEmpty(expireableCachePuts)) {
            ops = lazyInit(ops);
            for (ExpireableCachePut expireableCachePut : expireableCachePuts) {
                ops.add(parseExpireableCacheablePutAnnotation(ae, defaultConfig,
                        expireableCachePut));
            }
        }
        return ops;
    }

    private ExpireableCacheableOperation parseExpireableCacheablePutAnnotation(AnnotatedElement ae,
            DefaultCacheConfig defaultConfig, ExpireableCachePut expireableCachePut) {
        ExpireableCacheableOperation.Builder builder = new ExpireableCacheableOperation.Builder();
        builder.setName(ae.toString());
        builder.setCacheNames(expireableCachePut.cacheNames());
        builder.setCondition(expireableCachePut.condition());
        builder.setUnless(expireableCachePut.unless());
        builder.setKey(expireableCachePut.key());
        builder.setKeyGenerator(expireableCachePut.keyGenerator());
        builder.setCacheManager(expireableCachePut.cacheManager());
        builder.setCacheResolver(expireableCachePut.cacheResolver());
        defaultConfig.applyDefault(builder);
        ExpireableCacheableOperation op = builder.build();
        validateCacheOperation(ae, op);
        return op;
    }

    private ExpireableCacheableOperation parseExpireableCacheableAnnotation(AnnotatedElement ae,
            DefaultCacheConfig cachingConfig, ExpireableCacheable expireableCacheable) {
        ExpireableCacheableOperation.Builder builder = new ExpireableCacheableOperation.Builder();
        builder.setName(ae.toString());
        builder.setCacheNames(expireableCacheable.cacheNames());
        builder.setCondition(expireableCacheable.condition());
        builder.setUnless(expireableCacheable.unless());
        builder.setKey(expireableCacheable.key());
        builder.setKeyGenerator(expireableCacheable.keyGenerator());
        builder.setCacheManager(expireableCacheable.cacheManager());
        builder.setCacheResolver(expireableCacheable.cacheResolver());
        builder.setSync(expireableCacheable.sync());
        // 添加过期时间
        builder.setExpire(expireableCacheable.expire());
        builder.setTimeUnit(expireableCacheable.timeUnit());
        cachingConfig.applyDefault(builder);
        ExpireableCacheableOperation op = builder.build();
        validateCacheOperation(ae, op);
        return op;
    }

    private void validateCacheOperation(AnnotatedElement ae, CacheOperation operation) {
        if (StringUtils.hasText(operation.getKey())
                && StringUtils.hasText(operation.getKeyGenerator())) {
            throw new IllegalStateException("Invalid cache annotation configuration on '"
                    + ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. "
                    + "These attributes are mutually exclusive: either set the SpEL expression used to"
                    + "compute the key at runtime or set the name of the KeyGenerator bean to use.");
        }
        if (StringUtils.hasText(operation.getCacheManager())
                && StringUtils.hasText(operation.getCacheResolver())) {
            throw new IllegalStateException(
                    "Invalid cache annotation configuration on '" + ae.toString()
                            + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. "
                            + "These attributes are mutually exclusive: the cache manager is used to configure a"
                            + "default cache resolver if none is set. If a cache resolver is set, the cache manager"
                            + "won't be used.");
        }
    }
}
