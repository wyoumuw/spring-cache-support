package com.youmu.maven.springframework.cache.support.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheManager;

/**
 * @Author: YLBG-LDH-1506
 * @Description:
 * @Date: 2017/09/26
 */
public abstract class AbstractRedisCacheConfig implements CachingConfigurer {

	@Autowired
	RedisCacheManager redisCacheManager;

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

}