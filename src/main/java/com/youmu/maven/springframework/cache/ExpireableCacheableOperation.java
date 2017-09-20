package com.youmu.maven.springframework.cache;

import com.youmu.maven.springframework.cache.utils.CacheUtils;
import org.springframework.cache.interceptor.CacheableOperation;

import java.util.concurrent.TimeUnit;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/18
 */
public class ExpireableCacheableOperation extends CacheableOperation implements Expireable {

    private long expire;

    private TimeUnit timeUnit;

    public ExpireableCacheableOperation(Builder b) {
        super(b);
        this.expire = b.expire;
        this.timeUnit = b.timeUnit;
    }

    @Override
    public long getExpire() {
        return expire;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public static class Builder extends CacheableOperation.Builder {

        private long expire;

        private TimeUnit timeUnit;

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        @Override
        protected StringBuilder getOperationDescription() {
            return CacheUtils.addExpireableDesc(super.getOperationDescription(), this.expire,
                    this.timeUnit);
        }

        @Override
        public ExpireableCacheableOperation build() {
            return new ExpireableCacheableOperation(this);
        }
    }
}
