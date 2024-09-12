package cn.iecas.simulate.assessment.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;



@Configuration
public class CacheManagerConfig {

    @Autowired
    private ResourceLoader resourceLoader;


    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(getDefaultCacheConfiguration().entryTtl(Duration.ofHours(3))) // 默认的缓存配置，设置过期时间为3h
                //.withInitialCacheConfigurations(getCacheConfigurations()) // 自定义的缓存配置
                .transactionAware() // 在spring事务提交时才进行数据缓存
                .build();

    }


    /**
     * 自定义缓存Key生成器
     * @return
     */
    @Bean(value = "myKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... objects) {
                return method.getName() + Arrays.asList(objects).toString();
            }
        };
    }


    private RedisCacheConfiguration getDefaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig(resourceLoader.getClassLoader());
    }


    private RedisCacheConfiguration getDefaultCacheConfiguration(Duration ttl) {
        RedisCacheConfiguration customCacheConfiguration = this.getDefaultCacheConfiguration();
        customCacheConfiguration.entryTtl(ttl);
        customCacheConfiguration.disableKeyPrefix();
        customCacheConfiguration.disableCachingNullValues();
        customCacheConfiguration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        customCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        return customCacheConfiguration;
    }


    private Map<String, RedisCacheConfiguration> getCacheConfigurations() {
        Map<String, RedisCacheConfiguration> keyConfigMap = new HashMap<>();
        keyConfigMap.put("", this.getDefaultCacheConfiguration(Duration.ofSeconds(1)));
        return keyConfigMap;
    }
}
