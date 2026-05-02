package com.finance.dashboard.controller;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.response.ApiResponse;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Admin-only endpoints for managing the Redis cache at runtime.
 *
 * Useful when:
 *   - Data was updated directly in MySQL (bypassing the API)
 *   - A cache poisoning bug needs recovery without a full restart
 *   - QA wants to force a fresh fetch during testing
 *
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    private final CacheManager cacheManager;

    private static final List<String> ALL_CACHES = List.of(
            CacheConstants.DASHBOARD_SUMMARY,
            CacheConstants.DASHBOARD_TRENDS,
            CacheConstants.CATEGORY_TOTALS,
            CacheConstants.TRANSACTION_BY_ID,
            CacheConstants.USER_BY_ID,
            CacheConstants.PAYMENT_BY_ID
    );

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * DELETE /api/cache/all
     * Wipes every cache. Use when a bulk data import was done directly on MySQL.
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearAll() {
        ALL_CACHES.forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });

        return ResponseEntity.ok(ApiResponse.success(
                "All caches cleared",
                Map.of("clearedCaches", ALL_CACHES, "count", ALL_CACHES.size())
        ));
    }

    /**
     * DELETE /api/cache/{cacheName}
     * Clears a specific named cache.
     * Valid names: dashboard_summary, dashboard_trends, category_totals,
     *              transaction_by_id, user_by_id, payment_by_id
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<ApiResponse<Map<String, String>>> clearByName(
            @PathVariable String cacheName) {

        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Unknown cache: '" + cacheName
                            + "'. Valid names: " + ALL_CACHES));
        }
        cache.clear();
        return ResponseEntity.ok(ApiResponse.success(
                "Cache cleared",
                Map.of("cache", cacheName, "status", "cleared")
        ));
    }

    /**
     * DELETE /api/cache/transaction/{id}
     * Evicts a single transaction entry by ID.
     * Use after a direct DB fix on a specific transaction row.
     */
    @DeleteMapping("/transaction/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evictTransaction(
            @PathVariable Long id) {
        var cache = cacheManager.getCache(CacheConstants.TRANSACTION_BY_ID);
        if (cache != null) cache.evict(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction cache evicted",
                Map.of("transactionId", id, "cache", CacheConstants.TRANSACTION_BY_ID)
        ));
    }

    /**
     * DELETE /api/cache/user/{id}
     * Evicts a single user entry by ID.
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evictUser(
            @PathVariable Long id) {
        var cache = cacheManager.getCache(CacheConstants.USER_BY_ID);
        if (cache != null) cache.evict(id);
        return ResponseEntity.ok(ApiResponse.success(
                "User cache evicted",
                Map.of("userId", id, "cache", CacheConstants.USER_BY_ID)
        ));
    }
}
