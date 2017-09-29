/*
 * Copyright 2002-2012 the original author or authors. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.youmu.maven.springframework.cache.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.youmu.maven.springframework.cache.interceptor.handler.CacheHandler;
import com.youmu.maven.springframework.cache.interceptor.handler.DefaultCacheHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.lang.UsesJava8;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.youmu.maven.springframework.cache.exception.VariableNotAvailableException;

/**
 * @from spring-context
 * @since 3.1
 */
@SuppressWarnings("serial")
public class CustomableCacheInterceptor
        extends org.springframework.cache.interceptor.CacheInterceptor
        implements MethodInterceptor, Serializable {
    private static Class<?> javaUtilOptionalClass = null;

    static {
        try {
            javaUtilOptionalClass = ClassUtils.forName("java.util.Optional",
                    CustomableCacheInterceptor.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Java 8 not available - Optional references simply not supported
            // then.
        }
    }

    // do not serialize cache
    private final transient Map<CacheOperationCacheKey, CacheOperationMetadata> metadataCache = new ConcurrentHashMap<>(
            1024);

    // private final CacheOperationExpressionEvaluator evaluator = new
    // CacheOperationExpressionEvaluator();

    private COEEContainer coeeContainer = new COEEContainer();

    private static final CacheHandler DEFAULT_CACHE_HANDLER = new DefaultCacheHandler();

    private boolean initialized = false;

    protected CustomableCacheOperationContext getOperationContext(CacheOperation operation,
            Method method, Object[] args, Object target, Class<?> targetClass) {
        CacheOperationMetadata metadata = getCacheOperationMetadata(operation, method, targetClass);
        return new CustomableCacheOperationContext(metadata, args, target);
    }

    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();
        this.initialized = true;
    }

    /**
     * Return the {@link CacheOperationMetadata} for the specified operation.
     * <p>
     * Resolve the {@link CacheResolver} and the {@link KeyGenerator} to be used
     * for the operation.
     * @param operation the operation
     * @param method the method on which the operation is invoked
     * @param targetClass the target type
     * @return the resolved metadata for the operation
     */
    protected CacheOperationMetadata getCacheOperationMetadata(CacheOperation operation,
            Method method, Class<?> targetClass) {

        CacheOperationCacheKey cacheKey = new CacheOperationCacheKey(operation, method,
                targetClass);
        CacheOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            KeyGenerator operationKeyGenerator;
            if (StringUtils.hasText(operation.getKeyGenerator())) {
                operationKeyGenerator = getBean(operation.getKeyGenerator(), KeyGenerator.class);
            } else {
                operationKeyGenerator = getKeyGenerator();
            }
            CacheResolver operationCacheResolver;
            if (StringUtils.hasText(operation.getCacheResolver())) {
                operationCacheResolver = getBean(operation.getCacheResolver(), CacheResolver.class);
            } else if (StringUtils.hasText(operation.getCacheManager())) {
                CacheManager cacheManager = getBean(operation.getCacheManager(),
                        CacheManager.class);
                operationCacheResolver = new SimpleCacheResolver(cacheManager);
            } else {
                operationCacheResolver = getCacheResolver();
            }
            metadata = new CacheOperationMetadata(operation, method, targetClass,
                    operationKeyGenerator, operationCacheResolver);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    /**
     * Clear the cached metadata.
     */
    protected void clearMetadataCache() {
        this.metadataCache.clear();
        super.clearMetadataCache();
    }

    protected Object execute(CacheOperationInvoker invoker, Object target, Method method,
            Object[] args) {
        // Check whether aspect is enabled (to cope with cases where the AJ is
        // pulled in automatically)
        if (this.initialized) {
            Class<?> targetClass = getTargetClass(target);
            Collection<CacheOperation> operations = getCacheOperationSource()
                    .getCacheOperations(method, targetClass);
            if (!CollectionUtils.isEmpty(operations)) {
                return execute(invoker, method,
                        new CacheOperationContexts(operations, method, args, target, targetClass));
            }
        }

        return invoker.invoke();
    }

    private Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null && target != null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    private Object execute(final CacheOperationInvoker invoker, Method method,
            CacheOperationContexts contexts) {
        // Special handling of synchronized invocation
        if (contexts.isSynchronized()) {
            CustomableCacheOperationContext context = contexts.get(CacheableOperation.class)
                    .iterator().next();
            if (isConditionPassing(context, coeeContainer.getNoResult())) {
                Object key = generateKey(context, coeeContainer.getNoResult());
                Cache cache = context.getCaches().iterator().next();
                try {
                    return wrapCacheValue(method,
                            getCacheHandler().get(key, new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    return unwrapReturnValue(invokeOperation(invoker));
                                }
                            }, cache, context.getOperation()));
                } catch (Cache.ValueRetrievalException ex) {
                    // The invoker wraps any Throwable in a ThrowableWrapper
                    // instance so we
                    // can just make sure that one bubbles up the stack.
                    throw (CacheOperationInvoker.ThrowableWrapper) ex.getCause();
                }
            } else {
                // No caching required, only call the underlying method
                return invokeOperation(invoker);
            }
        }

        processCacheEvicts(contexts.get(CacheEvictOperation.class), true,
                coeeContainer.getNoResult());

        // Check if we have a cached item matching the conditions
        Cache.ValueWrapper cacheHit = findCachedItem(contexts.get(CacheableOperation.class));

        // Collect puts from any @Cacheable miss, if no cached item is found
        List<CachePutRequest> cachePutRequests = new LinkedList<CachePutRequest>();
        if (cacheHit == null) {
            collectPutRequests(contexts.get(CacheableOperation.class), coeeContainer.getNoResult(),
                    cachePutRequests);
        }

        Object cacheValue;
        Object returnValue;

        if (cacheHit != null && cachePutRequests.isEmpty() && !hasCachePut(contexts)) {
            // If there are no put requests, just use the cache hit
            cacheValue = cacheHit.get();
            returnValue = wrapCacheValue(method, cacheValue);
        } else {
            // Invoke the method if we don't have a cache hit
            returnValue = invokeOperation(invoker);
            cacheValue = unwrapReturnValue(returnValue);
        }

        // Collect any explicit @CachePuts
        collectPutRequests(contexts.get(CachePutOperation.class), cacheValue, cachePutRequests);

        // Process any collected put requests, either from @CachePut or a
        // @Cacheable miss
        for (CachePutRequest cachePutRequest : cachePutRequests) {
            cachePutRequest.apply(cacheValue);
        }

        // Process any late evictions
        processCacheEvicts(contexts.get(CacheEvictOperation.class), false, cacheValue);

        return returnValue;
    }

    protected CacheHandler getCacheHandler() {
        return null;
    }

    private Object wrapCacheValue(Method method, Object cacheValue) {
        if (method.getReturnType() == javaUtilOptionalClass
                && (cacheValue == null || cacheValue.getClass() != javaUtilOptionalClass)) {
            return OptionalUnwrapper.wrap(cacheValue);
        }
        return cacheValue;
    }

    private Object unwrapReturnValue(Object returnValue) {
        if (returnValue != null && returnValue.getClass() == javaUtilOptionalClass) {
            return OptionalUnwrapper.unwrap(returnValue);
        }
        return returnValue;
    }

    private boolean hasCachePut(CacheOperationContexts contexts) {
        // Evaluate the conditions *without* the result object because we don't
        // have it yet...
        Collection<CustomableCacheOperationContext> cachePutContexts = contexts
                .get(CachePutOperation.class);
        Collection<CustomableCacheOperationContext> excluded = new ArrayList<>();
        for (CustomableCacheOperationContext context : cachePutContexts) {
            try {
                if (!context.isConditionPassing(coeeContainer.getResultUnavailable())) {
                    excluded.add(context);
                }
            } catch (VariableNotAvailableException ex) {
                // Ignoring failure due to missing result, consider the cache
                // put has to proceed
            }
        }
        // Check if all puts have been excluded by condition
        return (cachePutContexts.size() != excluded.size());
    }

    private void processCacheEvicts(Collection<CustomableCacheOperationContext> contexts,
            boolean beforeInvocation, Object result) {
        for (CustomableCacheOperationContext context : contexts) {
            CacheEvictOperation operation = (CacheEvictOperation) context.getOperation();
            if (beforeInvocation == operation.isBeforeInvocation()
                    && isConditionPassing(context, result)) {
                performCacheEvict(context, operation, result);
            }
        }
    }

    private void performCacheEvict(CustomableCacheOperationContext context,
            CacheEvictOperation operation, Object result) {
        Object key = null;
        for (Cache cache : context.getCaches()) {
            if (operation.isCacheWide()) {
                logInvalidating(context, operation, null);
                doClear(cache);
            } else {
                if (key == null) {
                    key = context.generateKey(result);
                }
                logInvalidating(context, operation, key);
                doEvict(cache, key);
            }
        }
    }

    private void logInvalidating(CustomableCacheOperationContext context,
            CacheEvictOperation operation, Object key) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Invalidating " + (key != null ? "cache key [" + key + "]" : "entire cache")
                            + " for operation " + operation + " on method " + context.getMethod());
        }
    }

    /**
     * Find a cached item only for {@link CacheableOperation} that passes the
     * condition.
     * @param contexts the cacheable operations
     * @return a {@link Cache.ValueWrapper} holding the cached item, or
     *         {@code null} if none is found
     */
    private Cache.ValueWrapper findCachedItem(
            Collection<CustomableCacheOperationContext> contexts) {
        Object result = coeeContainer.getNoResult();
        for (CustomableCacheOperationContext context : contexts) {
            if (isConditionPassing(context, result)) {
                Object key = generateKey(context, result);
                Cache.ValueWrapper cached = findInCaches(context, key);
                if (cached != null) {
                    return cached;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("No cache entry for key '" + key + "' in cache(s) "
                                + context.getCacheNames());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Collect the {@link CachePutRequest} for all {@link CacheOperation} using
     * the specified result item.
     * @param contexts the contexts to handle
     * @param result the result item (never {@code null})
     * @param putRequests the collection to update
     */
    private void collectPutRequests(Collection<CustomableCacheOperationContext> contexts,
            Object result, Collection<CachePutRequest> putRequests) {

        for (CustomableCacheOperationContext context : contexts) {
            if (isConditionPassing(context, result)) {
                Object key = generateKey(context, result);
                putRequests.add(new CachePutRequest(context, key));
            }
        }
    }

    private Cache.ValueWrapper findInCaches(CustomableCacheOperationContext context, Object key) {
        for (Cache cache : context.getCaches()) {
            Cache.ValueWrapper wrapper = doGet(cache, key, context.getOperation());
            if (wrapper != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cache entry for key '" + key + "' found in cache '"
                            + cache.getName() + "'");
                }
                return wrapper;
            }
        }
        return null;
    }

    protected Cache.ValueWrapper doGet(Cache cache, Object key, CacheOperation operation) {
        try {
            return getCacheHandler().get(key, cache, operation);
        } catch (RuntimeException ex) {
            getErrorHandler().handleCacheGetError(ex, cache, key);
            return null; // If the exception is handled, return a cache miss
        }
    }

    private boolean isConditionPassing(CustomableCacheOperationContext context, Object result) {
        boolean passing = context.isConditionPassing(result);
        if (!passing && logger.isTraceEnabled()) {
            logger.trace("Cache condition failed on method " + context.getMethod()
                    + " for operation " + context.getOperation());
        }
        return passing;
    }

    private Object generateKey(CustomableCacheOperationContext context, Object result) {
        Object key = context.generateKey(result);
        if (key == null) {
            throw new IllegalArgumentException(
                    "Null key returned for cache operation (maybe you are "
                            + "using named params on classes without debug info?) "
                            + context.getOperation());
        }
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Computed cache key '" + key + "' for operation " + context.getOperation());
        }
        return key;
    }

    public class CacheOperationContexts {

        private final MultiValueMap<Class<? extends CacheOperation>, CustomableCacheOperationContext> contexts = new LinkedMultiValueMap<>();

        private final boolean sync;

        public CacheOperationContexts(Collection<? extends CacheOperation> operations,
                Method method, Object[] args, Object target, Class<?> targetClass) {
            for (CacheOperation operation : operations) {
                if (ClassUtils.isAssignable(CacheableOperation.class, operation.getClass())) {
                    this.contexts.add(CacheableOperation.class,
                            getOperationContext(operation, method, args, target, targetClass));
                } else if (ClassUtils.isAssignable(CachePutOperation.class, operation.getClass())) {
                    this.contexts.add(CachePutOperation.class,
                            getOperationContext(operation, method, args, target, targetClass));
                }
                this.contexts.add(operation.getClass(),
                        getOperationContext(operation, method, args, target, targetClass));
            }
            this.sync = determineSyncFlag(method);
        }

        public Collection<CustomableCacheOperationContext> get(
                Class<? extends CacheOperation> operationClass) {
            Collection<CustomableCacheOperationContext> result = this.contexts.get(operationClass);
            return (result != null ? result : Collections.emptyList());
        }

        public boolean isSynchronized() {
            return this.sync;
        }

        private boolean determineSyncFlag(Method method) {
            List<CustomableCacheOperationContext> cacheOperationContexts = this.contexts
                    .get(CacheableOperation.class);
            if (cacheOperationContexts == null) { // no @Cacheable operation at
                // all
                return false;
            }
            boolean syncEnabled = false;
            for (CacheOperationContext cacheOperationContext : cacheOperationContexts) {
                if (((CacheableOperation) cacheOperationContext.getOperation()).isSync()) {
                    syncEnabled = true;
                    break;
                }
            }
            if (syncEnabled) {
                if (this.contexts.size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) cannot be combined with other cache operations on '"
                                    + method + "'");
                }
                if (cacheOperationContexts.size() > 1) {
                    throw new IllegalStateException(
                            "Only one @Cacheable(sync=true) entry is allowed on '" + method + "'");
                }
                CustomableCacheOperationContext cacheOperationContext = cacheOperationContexts
                        .iterator().next();
                CacheableOperation operation = (CacheableOperation) cacheOperationContext
                        .getOperation();
                if (cacheOperationContext.getCaches().size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) only allows a single cache on '" + operation
                                    + "'");
                }
                if (StringUtils.hasText(operation.getUnless())) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) does not support unless attribute on '"
                                    + operation + "'");
                }
                return true;
            }
            return false;
        }
    }

    protected class CustomableCacheOperationContext extends CacheOperationContext {

        public CustomableCacheOperationContext(CacheOperationMetadata metadata, Object[] args,
                Object target) {
            super(metadata, args, target);
        }

        @Override
        protected Collection<? extends Cache> getCaches() {
            return super.getCaches();
        }

        @Override
        protected Object generateKey(Object result) {
            return super.generateKey(result);
        }

        @Override
        protected boolean canPutToCache(Object value) {
            return super.canPutToCache(value);
        }

        @Override
        protected boolean isConditionPassing(Object result) {
            return super.isConditionPassing(result);
        }

        @Override
        protected Collection<String> getCacheNames() {
            return super.getCacheNames();
        }
    }

    private class CachePutRequest {

        private final CustomableCacheOperationContext context;

        private final Object key;

        public CachePutRequest(CustomableCacheOperationContext context, Object key) {
            this.context = context;
            this.key = key;
        }

        public void apply(Object result) {
            if (this.context.canPutToCache(result)) {
                for (Cache cache : this.context.getCaches()) {
                    doPut(cache, this.key, result, context.getOperation());
                }
            }
        }
    }

    protected void doPut(Cache cache, Object key, Object result, CacheOperation operation) {
        try {
            getCacheHandler().put(key, result, cache, operation);
        } catch (RuntimeException ex) {
            getErrorHandler().handleCachePutError(ex, cache, key, result);
        }
    }

    private static final class CacheOperationCacheKey
            implements Comparable<CacheOperationCacheKey> {

        private final CacheOperation cacheOperation;

        private final AnnotatedElementKey methodCacheKey;

        private CacheOperationCacheKey(CacheOperation cacheOperation, Method method,
                Class<?> targetClass) {
            this.cacheOperation = cacheOperation;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheOperationCacheKey)) {
                return false;
            }
            CacheOperationCacheKey otherKey = (CacheOperationCacheKey) other;
            return (this.cacheOperation.equals(otherKey.cacheOperation)
                    && this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.cacheOperation.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.cacheOperation + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(CacheOperationCacheKey other) {
            int result = this.cacheOperation.getName().compareTo(other.cacheOperation.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }

    /**
     * Inner class to avoid a hard dependency on Java 8.
     */
    @UsesJava8
    private static class OptionalUnwrapper {

        public static Object unwrap(Object optionalObject) {
            Optional<?> optional = (Optional<?>) optionalObject;
            if (!optional.isPresent()) {
                return null;
            }
            Object result = optional.get();
            Assert.isTrue(!(result instanceof Optional),
                    "Multi-level Optional usage not supported");
            return result;
        }

        public static Object wrap(Object value) {
            return Optional.ofNullable(value);
        }
    }
}
