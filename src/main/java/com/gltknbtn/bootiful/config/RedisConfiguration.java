package com.gltknbtn.bootiful.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EnableCaching
@Configuration
public class RedisConfiguration extends CachingConfigurerSupport {

    @Autowired
    private CacheConfigurationProperties cacheConfigurationProperties;
    private String KEY_SEPERATOR = "#";

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {

            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(KEY_SEPERATOR);
            sb.append(method.getName());
            sb.append(KEY_SEPERATOR);
            for(Object param : params){
                sb.append(param.toString());
                sb.append(KEY_SEPERATOR);
            }

            String str = sb.toString();

            return str.substring(0, str.length() - 1);
        };
    }

    private org.springframework.data.redis.cache.RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        RedisSerializationContext.SerializationPair<Object> jsonSerializer = RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());
        return org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds))
                .serializeValuesWith(jsonSerializer);
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        if (Objects.nonNull(cacheConfigurationProperties.getCachesTTL())) {
            for (Map.Entry<String, String> cacheNameAndTimeout : cacheConfigurationProperties.getCachesTTL().entrySet()) {
                cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(Long.parseLong(cacheNameAndTimeout.getValue())));
            }
        }
        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(createCacheConfiguration(Long.parseLong(cacheConfigurationProperties.getDefaultTTL())))
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(cacheConfigurationProperties.getHost());
        redisStandaloneConfiguration.setPort(Integer.parseInt(cacheConfigurationProperties.getPort()));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Configuration
    @ConfigurationProperties(prefix = "cache")
    @Data
    class CacheConfigurationProperties {
        private String port;
        private String host;
        private String defaultTTL;
        private Map<String, String> cachesTTL;
    }

}