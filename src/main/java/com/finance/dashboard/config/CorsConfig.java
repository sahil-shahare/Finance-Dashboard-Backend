package com.finance.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the standalone HTML/JS frontend.
 *
 * Why CorsConfigurationSource (not CorsFilter):
 *
 * In Spring Boot 3 / Spring Security 6, a bare CorsFilter @Bean gets
 * auto-registered at the default servlet-filter order (0), which is
 * AFTER the Spring Security filter chain (order -100).  That means
 * Spring Security intercepts OPTIONS preflight requests and returns
 * 401/403 before the CorsFilter ever runs, so the browser never gets
 * CORS headers and blocks every request.
 *
 * Declaring a CorsConfigurationSource @Bean instead lets Spring
 * Security's own CORS support (enabled via .cors() in SecurityConfig)
 * handle preflight correctly — inside the security chain, before any
 * authentication checks.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from the frontend — file://, localhost, any port
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Register on /** so Spring Security's CORS filter matches every path,
        // including /api/auth/login which must work before a token exists.
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}