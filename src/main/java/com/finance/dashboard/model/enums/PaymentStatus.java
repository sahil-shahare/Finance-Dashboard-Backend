package com.finance.dashboard.model.enums;

/**
 * Tracks the lifecycle of a Razorpay payment.
 *
 * CREATED  → Order created on Razorpay, awaiting user payment.
 * SUCCESS  → Payment verified via HMAC signature — money received.
 * FAILED   → Payment attempted but failed (card declined, timeout, etc.)
 * REFUNDED → Payment was successfully refunded via Razorpay dashboard.
 */
public enum PaymentStatus {
    CREATED,
    SUCCESS,
    FAILED,
    REFUNDED
}
