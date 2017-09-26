package com.youmu.maven.springframework.cache.interceptor;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

import com.youmu.maven.springframework.cache.exception.ReflectionException;

/**
 * @Author: YOUMU
 * @Description: org.springframework.cache.interceptor.CacheOperationExpressionEvaluator
 * @Date: 2017/09/26
 */
public class COEEContainer {
    private Object noResult;
    private Object resultUnavailable;

    public COEEContainer() {
        Class clazz = null;
        try {
            clazz = Class.forName(
                    "org.springframework.cache.interceptor.CacheOperationExpressionEvaluator");
            Field noResultField = ReflectionUtils.findField(clazz, "NO_RESULT");
            ReflectionUtils.makeAccessible(noResultField);
            noResult = noResultField.get(clazz);
            Field resultUnavailableField = ReflectionUtils.findField(clazz, "RESULT_UNAVAILABLE");
            ReflectionUtils.makeAccessible(resultUnavailableField);
            resultUnavailable = resultUnavailableField.get(clazz);
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    public Object getNoResult() {
        return noResult;
    }

    public Object getResultUnavailable() {
        return resultUnavailable;
    }
}
