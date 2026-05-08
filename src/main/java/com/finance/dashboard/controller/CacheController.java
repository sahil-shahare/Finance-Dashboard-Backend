package com.finance.dashboard.controller;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    private static final Logger log = LoggerFactory.getLogger(CacheController.class);
    private final CacheManager cacheManager;

    private static final List<String> ALL_CACHES = List.of(
            CacheConstants.DASHBOARD_SUMMARY, CacheConstants.DASHBOARD_TRENDS,
            CacheConstants.CATEGORY_TOTALS,   CacheConstants.TRANSACTION_BY_ID,
            CacheConstants.USER_BY_ID,        CacheConstants.PAYMENT_BY_ID
    );

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearAll() {
        int cleared = 0, failed = 0;
        for (String name : ALL_CACHES) {
            try {
                var c = cacheManager.getCache(name);
                if (c != null) { c.clear(); cleared++; }
            } catch (Exception e) {
                failed++;
                log.warn("[Cache] clearAll failed for '{}': {}", name, e.getMessage());
            }
        }
        String msg = failed == 0
                ? "All " + cleared + " caches cleared"
                : cleared + " cleared, " + failed + " failed (Redis may be unavailable)";
        return ResponseEntity.ok(ApiResponse.success(msg,
                Map.of("cleared", cleared, "failed", failed)));
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearByName(
            @PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null)
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unknown cache: '" + cacheName + "'. Valid: " + ALL_CACHES));
        try {
            cache.clear();
            return ResponseEntity.ok(ApiResponse.success("Cache cleared",
                    Map.of("cache", cacheName, "status", "cleared")));
        } catch (Exception e) {
            log.warn("[Cache] clear failed for '{}': {}", cacheName, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(
                    "Skipped — Redis unavailable (entries expire via TTL)",
                    Map.of("cache", cacheName, "status", "redis_unavailable")));
        }
    }

    @DeleteMapping("/transaction/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evictTransaction(@PathVariable Long id) {
        try {
            var c = cacheManager.getCache(CacheConstants.TRANSACTION_BY_ID);
            if (c != null) c.evict(id);
            return ResponseEntity.ok(ApiResponse.success("Transaction evicted",
                    Map.of("transactionId", id)));
        } catch (Exception e) {
            log.warn("[Cache] evict transaction {} failed: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("Skipped — Redis unavailable",
                    Map.of("transactionId", id, "status", "redis_unavailable")));
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evictUser(@PathVariable Long id) {
        try {
            var c = cacheManager.getCache(CacheConstants.USER_BY_ID);
            if (c != null) c.evict(id);
            return ResponseEntity.ok(ApiResponse.success("User evicted",
                    Map.of("userId", id)));
        } catch (Exception e) {
            log.warn("[Cache] evict user {} failed: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("Skipped — Redis unavailable",
                    Map.of("userId", id, "status", "redis_unavailable")));
        }
    }
}