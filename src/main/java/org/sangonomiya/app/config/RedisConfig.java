package org.sangonomiya.app.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis阵列配置
 * @author Dioxide.CN
 * @date 2023/3/2 7:40
 * @since 1.0
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    public static final String SPLITS = ":"; // 分割符
    public static final String APP_PREFIX = "redis"; // 应用前缀
    public static final String CACHE_NAMES_PREFIX = APP_PREFIX + SPLITS + "cacheNames" + SPLITS; // 缓存前缀redis:cacheNames:

    public static final String CACHE_NAME_FOREVER = CACHE_NAMES_PREFIX + "forever"; // 永不过期标识符
    public static final String CACHE_NAME_MINUTES_10 = CACHE_NAMES_PREFIX + "minutes-10"; // 10分钟缓存
    public static final String CACHE_NAME_MINUTES_30 = CACHE_NAMES_PREFIX + "minutes-30"; // 30分钟缓存
    public static final String CACHE_NAME_HOURS_01 = CACHE_NAMES_PREFIX + "hours-1"; // 1小时有效期的缓存名
    public static final String CACHE_NAME_HOURS_12 = CACHE_NAMES_PREFIX + "hours-12"; // 12小时有效期的缓存名
    public static final String CACHE_NAME_HOURS_24 = CACHE_NAMES_PREFIX + "hours-24"; // 24小时有效期的缓存名
    public static final String CACHE_NAME_DAYS_30 = CACHE_NAMES_PREFIX + "days-30"; // 30天有效期的缓存名

    /**
     * 选择redis作为默认缓存工具
     * SpringBoot2.0以上CacheManager配置方式
     * @param redisTemplate redisTemplate Bean
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置key为String
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getStringSerializer()))
                // 设置value 为自动转Json的Object
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()))
                // 不缓存null
                .disableCachingNullValues()
                // 缓存数据保存1小时
                .entryTtl(Duration.ofHours(1));

        // RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        return RedisCacheManager.RedisCacheManagerBuilder
                // Redis 连接工厂
                .fromConnectionFactory(
                        Objects.requireNonNull(redisTemplate.getConnectionFactory()))
                // 缓存配置
                .cacheDefaults(defaultCacheConfiguration)
                // 配置同步修改或删除 put/evict
                .transactionAware()
                .build();
    }

    /**
     * redisTemplate相关配置
     * @param factory RedisConnectionFactory工厂
     * @return RedisTemplate Bean
     */
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer<Object> jacksonSerial = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSerial.setObjectMapper(om);

        // 值采用json序列化
        template.setValueSerializer(jacksonSerial);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());

        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerial);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 对hash类型的数据操作
     * @param redisTemplate RedisTemplate Bean
     * @return HashOperations Bean
     */
    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 对redis字符串类型数据操作
     * @param redisTemplate RedisTemplate Bean
     * @return ValueOperations Bean
     */
    @Bean
    public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 对链表类型的数据操作
     * @param redisTemplate RedisTemplate Bean
     * @return ListOperations Bean
     */
    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 对无序集合类型的数据操作
     * @param redisTemplate RedisTemplate Bean
     * @return SetOperations Bean
     */
    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 对有序集合类型的数据操作
     * @param redisTemplate RedisTemplate Bean
     * @return ZSetOperations Bean
     */
    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }

}
