package com.finance.dashboard.model.enums;

/**
 * Defines the access tiers for users in the system.
 *
 * VIEWER  → Read-only: can view transactions and the basic dashboard summary.
 * ANALYST → Read + insights: all VIEWER access plus category totals and monthly trends.
 * ADMIN   → Full access: create / update / delete transactions and manage all users.
 */
public enum Role {
    VIEWER,
    ANALYST,
    ADMIN
}
