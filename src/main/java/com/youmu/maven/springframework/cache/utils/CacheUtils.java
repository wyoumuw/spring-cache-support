package com.youmu.maven.springframework.cache.utils;

import java.util.concurrent.TimeUnit;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public abstract class CacheUtils {

    public static StringBuilder addExpireableDesc(StringBuilder raw, long expire,
            TimeUnit timeUnit) {
        StringBuilder sb = null == raw ? new StringBuilder() : raw;
        sb.append(" | expire='");
        sb.append(expire);
        sb.append("'");
        sb.append(" | timeUnit='");
        sb.append(timeUnit);
        sb.append("'");
        return sb;
    }
}
