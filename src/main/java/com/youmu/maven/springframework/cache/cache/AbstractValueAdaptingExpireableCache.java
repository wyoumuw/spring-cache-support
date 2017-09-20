package com.youmu.maven.springframework.cache.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2017/09/19
 */
public abstract class AbstractValueAdaptingExpireableCache extends AbstractValueAdaptingCache implements ExpireableCache {
	/**
	 * Create an {@code AbstractValueAdaptingCache} with the given setting.
	 *
	 * @param allowNullValues whether to allow for {@code null} values
	 */
	protected AbstractValueAdaptingExpireableCache(boolean allowNullValues) {
		super(allowNullValues);
	}
}
