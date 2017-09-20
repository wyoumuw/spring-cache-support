package com.youmu.maven.springframework.cache.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.util.ClassUtils;

/**
 * @from spring-context
 * @see ProxyCachingConfiguration
 */
public class CustomableCachingConfigurationSelector
        extends AdviceModeImportSelector<EnableCustomableCache> {

    private static final String PROXY_JCACHE_CONFIGURATION_CLASS = "org.springframework.cache.jcache.config.ProxyJCacheConfiguration";

    private static final String CACHE_ASPECT_CONFIGURATION_CLASS_NAME = "org.springframework.cache.aspectj.AspectJCachingConfiguration";

    private static final String JCACHE_ASPECT_CONFIGURATION_CLASS_NAME = "org.springframework.cache.aspectj.AspectJJCacheConfiguration";

    private static final boolean jsr107Present = ClassUtils.isPresent("javax.cache.Cache",
            CustomableCachingConfigurationSelector.class.getClassLoader());

    private static final boolean jcacheImplPresent = ClassUtils.isPresent(
            PROXY_JCACHE_CONFIGURATION_CLASS,
            CustomableCachingConfigurationSelector.class.getClassLoader());

    @Override
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
        case PROXY:
            return getProxyImports();
        case ASPECTJ:
            return getAspectJImports();
        default:
            return null;
        }
    }

    private String[] getProxyImports() {
        List<String> result = new ArrayList<String>();
        result.add(AutoProxyRegistrar.class.getName());
        result.add(getProxyConfig().getName());
        if (jsr107Present && jcacheImplPresent) {
            result.add(PROXY_JCACHE_CONFIGURATION_CLASS);
        }
        return result.toArray(new String[result.size()]);
    }

    protected  Class<? extends CacheConfig> getProxyConfig(){
    	return CacheConfig.class;
	}

	/**
	 * unsupported
	 * @return
	 */
	private String[] getAspectJImports() {
		throw new UnsupportedOperationException("unsupported aspectj cache");
        // List<String> result = new ArrayList<String>();
        // result.add(CACHE_ASPECT_CONFIGURATION_CLASS_NAME);
        // if (jsr107Present && jcacheImplPresent) {
        // result.add(JCACHE_ASPECT_CONFIGURATION_CLASS_NAME);
        // }
        // return result.toArray(new String[result.size()]);
    }

}
