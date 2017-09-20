package com.youmu.maven.springframework.cache.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/16
 */
public class LogOnlyCacheErrorHandler implements CacheErrorHandler {

    private static Logger logger = LoggerFactory.getLogger(LogOnlyCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logger.error(">>>>>>>>>>handleCacheGetError: key " + key, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key,
									Object value) {
        logger.error(">>>>>>>>>>handleCachePutError: key " + key + " value:" + value, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logger.error(">>>>>>>>>>handleCacheEvictError: key " + key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logger.error(">>>>>>>>>>RuntimeException", exception);
    }
}
