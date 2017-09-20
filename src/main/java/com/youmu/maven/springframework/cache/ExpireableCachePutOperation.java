package com.youmu.maven.springframework.cache;

import com.youmu.maven.springframework.cache.utils.CacheUtils;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;

import java.util.concurrent.TimeUnit;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public class ExpireableCachePutOperation extends CachePutOperation implements Expireable {
    private long expire;

    private TimeUnit timeUnit;

    public ExpireableCachePutOperation(ExpireableCachePutOperation.Builder b) {
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

    public static class Builder extends CachePutOperation.Builder {

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
        public ExpireableCachePutOperation build() {
            return new ExpireableCachePutOperation(this);
        }
    }
}
