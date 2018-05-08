package io.csra.wily.components.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * A redis cache configuration that will use the properties redis.url and redis.port to establish a connection.
 * Setting those properties in any properties file on the classpath will allow the application to bootstrap.
 *
 * You must extend this implementation in your local project to get an out-of-the-box configuration.
 *
 * @author ndimola
 *
 */
public class MasterCacheConfiguration {

	@Autowired
	private Environment environment;

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().build();
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(environment.getRequiredProperty("redis.url"), Integer.valueOf(environment.getRequiredProperty("redis.port")));
		return new JedisConnectionFactory(redisConfig, clientConfig);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory);

		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisTemplate<String, String> redisTemplate) {
		return RedisCacheManager.create(redisTemplate.getConnectionFactory());
	}	
	
}