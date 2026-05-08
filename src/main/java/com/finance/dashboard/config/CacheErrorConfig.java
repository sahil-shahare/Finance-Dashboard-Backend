package com.finance.dashboard.config;

/**
 * This file is intentionally empty.
 *
 * The CacheErrorHandler is now correctly registered inside RedisConfig
 * by implementing CachingConfigurer and overriding errorHandler().
 *
 * A standalone @Bean CacheErrorHandler method (previous approach) is
 * silently ignored by Spring — it must come from CachingConfigurer.
 */
public class CacheErrorConfig {
    // Intentionally empty — see RedisConfig.errorHandler()
}