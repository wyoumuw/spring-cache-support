package com.youmu.maven.springframework.cache.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: YOUMU
 * @Description: just support java proxy
 * @Date: 2017/09/20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableCaching
@Import(CacheConfig.class)
public @interface EnableCusomableCache {
}
