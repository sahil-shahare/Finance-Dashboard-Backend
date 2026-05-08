package com.finance.dashboard.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
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
 * Redis configuration with graceful fallback.
 *
 * Implements CachingConfigurer so errorHandler() is properly registered
 * by Spring — a standalone @Bean CacheErrorHandler is NOT picked up.
 *
 * When Redis is unavailable:
 *   - @Cacheable  → method executes normally, result not cached (DB hit every time)
 *   - @CachePut   → method executes normally, result not cached
 *   - @CacheEvict → eviction silently skipped, stale data may remain until TTL expires
 *   - CacheController direct calls → 200 OK with warning message, no crash
 *
 * The app stays fully functional with just MySQL — Redis is an optional performance layer.
 */
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    // ── RedisTemplate ─────────────────────────────────────────────────────────

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

    // ── CacheManager ──────────────────────────────────────────────────────────

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();
        perCacheConfig.put(CacheConstants.DASHBOARD_SUMMARY,  defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put(CacheConstants.DASHBOARD_TRENDS,   defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put(CacheConstants.CATEGORY_TOTALS,    defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put(CacheConstants.TRANSACTION_BY_ID,  defaultConfig.entryTtl(Duration.ofMinutes(5)));
        perCacheConfig.put(CacheConstants.USER_BY_ID,         defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put(CacheConstants.PAYMENT_BY_ID,      defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }

    // ── CacheErrorHandler — registered via CachingConfigurer ─────────────────
    //
    // This is the CORRECT way to register a custom error handler.
    // A standalone @Bean CacheErrorHandler method is IGNORED by Spring.

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("[Cache] GET failed — cache='{}' key='{}' — serving from DB. Cause: {}",
                        cache.getName(), key, rootCause(e));
                // Swallow: Spring will call the actual @Cacheable method instead
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("[Cache] PUT failed — cache='{}' key='{}' — result not cached. Cause: {}",
                        cache.getName(), key, rootCause(e));
                // Swallow: response still returned to client, just not cached
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("[Cache] EVICT failed — cache='{}' key='{}' — stale data may persist until TTL. Cause: {}",
                        cache.getName(), key, rootCause(e));
                // Swallow: eviction failure is non-fatal
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("[Cache] CLEAR failed — cache='{}' — Cause: {}",
                        cache.getName(), rootCause(e));
                // Swallow
            }

            private String rootCause(RuntimeException e) {
                Throwable cause = e;
                while (cause.getCause() != null) cause = cause.getCause();
                return cause.getClass().getSimpleName() + ": " + cause.getMessage();
            }
        };
    }

    // ── Jackson serialiser ────────────────────────────────────────────────────

    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}