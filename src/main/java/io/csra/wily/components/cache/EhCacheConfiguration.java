package io.csra.wily.components.cache;

import java.util.List;

import net.sf.ehcache.config.CacheConfiguration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;

/**
 * This is an in-memory cache solution. Generally, you should avoid using this in favor of a distributed cache, like
 * Redis, which is available in the MasterCacheConfiguration.
 *
 */
public abstract class EhCacheConfiguration implements CachingConfigurer {

	protected static final int DEFAULT_MAX_ENTRIES = 1000;
	protected static final String DEFAULT_EVICTION_POLICY = "LRU";
	protected static final int DEFAULT_HOURS_TO_LIVE = 4;

	/**
	 * Loops the provided cache configs and provides an instance of the EhCacheManager
	 * 
	 * @return
	 */
	@Bean(destroyMethod = "shutdown")
	public net.sf.ehcache.CacheManager ehCacheManager() {
		net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();

		for (CacheConfiguration cacheConfig : getCacheConfigurations()) {
			config.addCache(cacheConfig);
		}

		return net.sf.ehcache.CacheManager.newInstance(config);
	}

	/**
	 * Provide a list of all cache configurations available for your application. Use with buildCacheConfiguration() helper
	 * method to make cache creation easy.
	 * 
	 * Be sure to add @Configuration and @EnableCaching to your class in order to activate this cache.
	 * 
	 * @return
	 */
	protected abstract List<CacheConfiguration> getCacheConfigurations();

	/**
	 * Helper method to construct cache configurations. Provide the required fields and the associated CacheConfiguration
	 * will result.
	 * 
	 * @param cacheName
	 * @param evictionPolicy
	 * @param maxEntries
	 * @return
	 */
	protected CacheConfiguration buildCacheCongfiguration(String cacheName, String evictionPolicy, int maxEntries, int hoursToLive) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.setName(cacheName);
		cacheConfiguration.setMemoryStoreEvictionPolicy(evictionPolicy);
		cacheConfiguration.setMaxEntriesLocalHeap(maxEntries);
		cacheConfiguration.setTimeToLiveSeconds(hoursToLive * 60 * 60);

		return cacheConfiguration;
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager());
	}

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new SimpleKeyGenerator();
	}

	@Bean
	@Override
	public CacheResolver cacheResolver() {
		return new SimpleCacheResolver(cacheManager());
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new SimpleCacheErrorHandler();
	}

}