package com.finance.dashboard.config;

/**
 * Central registry of all cache names used in the application.
 *
 * Using constants avoids typos in @Cacheable / @CacheEvict annotations
 * and makes it easy to see every cache at a glance.
 *
 * TTL (Time-To-Live) per cache — configured in RedisConfig:
 *
 *   DASHBOARD_SUMMARY   → 10 minutes  (heavy aggregation, evicted on any transaction write)
 *   DASHBOARD_TRENDS    → 10 minutes  (12-month roll-up, evicted on any transaction write)
 *   CATEGORY_TOTALS     → 10 minutes  (category aggregation, evicted on any transaction write)
 *   TRANSACTION_BY_ID   →  5 minutes  (single record, evicted on update/delete)
 *   USER_BY_ID          → 30 minutes  (user profile changes rarely)
 *   PAYMENT_BY_ID       → 15 minutes  (payment records are mostly immutable after SUCCESS)
 */
public final class CacheConstants {

    private CacheConstants() {}

    public static final String DASHBOARD_SUMMARY = "dashboard_summary";
    public static final String DASHBOARD_TRENDS  = "dashboard_trends";
    public static final String CATEGORY_TOTALS   = "category_totals";
    public static final String TRANSACTION_BY_ID = "transaction_by_id";
    public static final String USER_BY_ID        = "user_by_id";
    public static final String PAYMENT_BY_ID     = "payment_by_id";
}
