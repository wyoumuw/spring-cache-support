package com.youmu.maven.springframework.cache;

import java.util.concurrent.TimeUnit;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public interface Expireable {
    public long getExpire();

    public TimeUnit getTimeUnit();
}
