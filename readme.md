#Exprieable spring cache 

##Use as XML
enable aop module 
        
        <aop:config />
        
if you had configured the other aop config so you can ignore above settings.

other aop config:
        
        <tx:annotation-driven transaction-manager="transactionManager"></tx:annotation-driven>
        
        <aop:aspectj-autoproxy ></aop:aspectj-autoproxy>
        
final configure the ExprieableConfig
...........to be continue.............


No! It's just finished spring data redis support, 

Xml config:

        ...here config your redisCacheManager or key gen or errorHandler
        
        <bean class="com.youmu.maven.springframework.cache.support.redis.SimpleRedisCacheConfig"></bean>
        
Annotation config:
        

        @Configuration
        @EnableCustomableCache
        public class MyConfig extends AbstractRedisCacheConfig {
        
        	@Bean
        	public RedisCacheManager redisCacheManager(){
        		RedisCacheManager redisCacheManager=new RedisCacheManager(redisTemplate());
        		redisCacheManager.setDefaultExpiration(100);
        		redisCacheManager.setUsePrefix(true);
        		return redisCacheManager;
        	}
        
        	@Bean
        	public JedisConnectionFactory jedisConnectionFactory(){
        		JedisConnectionFactory jedisConnectionFactory=new JedisConnectionFactory();
        		jedisConnectionFactory.setUsePool(true);
        		jedisConnectionFactory.setPassword("");
        		jedisConnectionFactory.setHostName("localhost");
        		jedisConnectionFactory.setPort(6379);
        		jedisConnectionFactory.setTimeout(10000);
        		return jedisConnectionFactory;
        	}
        	@Bean
        	public RedisTemplate redisTemplate(){
        		RedisTemplate redisTemplate=new RedisTemplate();
        		redisTemplate.setConnectionFactory(jedisConnectionFactory());
        		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        		return redisTemplate;
        	}
        
        	@Override
        	public CacheManager cacheManager() {
        		return redisCacheManager();
        	}
        
        	@Override
        	public KeyGenerator keyGenerator() {
        		return null;
        	}
        
        	@Override
        	public CacheErrorHandler errorHandler() {
        		return null;
        	}
        }


ok,so you just finish the config and use it just add a annotation to your service methods as:

        	@Cacheable("user")
        	public User user() throws InterruptedException {
        		System.out.println("check start");
        		Thread.sleep(1000);
        		System.out.println("check end");
        		return new User("youmu","11111");
        	}
        	
or

            @ExpireableCacheable(value = "user",expire = 10)
            public User user() throws InterruptedException {
                System.out.println("check start");
                Thread.sleep(1000);
                System.out.println("check end");
                return new User("youmu","11111");
            }
first above use default timeout, the second use expire 10 seconds