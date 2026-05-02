package com.finance.dashboard.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configures Spring's caching layer to use Redis as the backing store.
 *
 * Key decisions:
 *  - Values are serialised as JSON (not Java serialisation) so they are
 *    human-readable in Redis CLI and survive class refactors without cache
 *    poisoning.
 *  - Each cache has its own TTL — dashboard aggregations expire faster than
 *    user profiles, which change rarely.
 *  - A default TTL of 5 minutes applies to any cache not explicitly listed.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    // ── RedisTemplate ────────────────────────────────────────────────────────

    /**
     * General-purpose template for manual Redis operations (get/set/delete).
     * Keys   → plain UTF-8 strings
     * Values → JSON via Jackson (handles LocalDate, BigDecimal, etc.)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer());
        template.setHashValueSerializer(jsonSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ── CacheManager ─────────────────────────────────────────────────────────

    /**
     * Wires Spring's @Cacheable / @CacheEvict / @CachePut annotations to Redis.
     * Each cache gets a dedicated TTL; everything else falls back to the default.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))             // default TTL
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer()))
                .disableCachingNullValues();                 // never cache null

        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();

        // Dashboard aggregations — evicted on every transaction write, so TTL
        // is mostly a safety net. 10 min keeps data fresh if eviction is missed.
        perCacheConfig.put(CacheConstants.DASHBOARD_SUMMARY,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        perCacheConfig.put(CacheConstants.DASHBOARD_TRENDS,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        perCacheConfig.put(CacheConstants.CATEGORY_TOTALS,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Individual records — short TTL; evicted explicitly on update/delete
        perCacheConfig.put(CacheConstants.TRANSACTION_BY_ID,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Users change rarely — longer TTL is safe
        perCacheConfig.put(CacheConstants.USER_BY_ID,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Payments are mostly immutable after SUCCESS
        perCacheConfig.put(CacheConstants.PAYMENT_BY_ID,
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }

    // ── Serialiser ────────────────────────────────────────────────────────────

    /**
     * Jackson-based JSON serialiser that includes type metadata (@class field).
     * The type metadata is required so Jackson can deserialise back to the
     * correct concrete class without an explicit type hint at the call site.
     */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());         // LocalDate, LocalDateTime
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY                    // stored as "@class" field in JSON
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
