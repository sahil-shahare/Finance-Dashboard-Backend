package com.finance.dashboard.model;

import com.finance.dashboard.model.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persists every Razorpay payment attempt and its outcome.
 *
 * Lifecycle:
 *   1. POST /api/payments/create-order  → row inserted with status=CREATED
 *   2. POST /api/payments/verify        → row updated to SUCCESS or FAILED
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_razorpay_order",   columnList = "razorpay_order_id"),
        @Index(name = "idx_payment_razorpay_payment", columnList = "razorpay_payment_id"),
        @Index(name = "idx_payment_status",           columnList = "status"),
        @Index(name = "idx_payment_user",             columnList = "user_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Razorpay order ID returned by the create-order API (e.g. order_xxxxx) */
    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 100)
    private String razorpayOrderId;

    /** Razorpay payment ID sent by the client after a successful checkout (e.g. pay_xxxxx) */
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    /** HMAC-SHA256 signature returned by Razorpay — stored for audit purposes */
    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    /** Amount in the smallest currency unit — paise for INR (₹100 = 10000 paise) */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** ISO 4217 currency code. Default: INR */
    @Column(nullable = false, length = 10)
    private String currency;

    /** Human-readable purpose, e.g. "Premium Subscription" */
    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** The authenticated user who initiated this payment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Payment() {}

    private Payment(Builder b) {
        this.razorpayOrderId   = b.razorpayOrderId;
        this.razorpayPaymentId = b.razorpayPaymentId;
        this.razorpaySignature = b.razorpaySignature;
        this.amount            = b.amount;
        this.currency          = b.currency;
        this.description       = b.description;
        this.status            = b.status;
        this.user              = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private BigDecimal amount;
        private String currency = "INR";
        private String description;
        private PaymentStatus status;
        private User user;

        public Builder razorpayOrderId(String v)   { this.razorpayOrderId = v; return this; }
        public Builder razorpayPaymentId(String v) { this.razorpayPaymentId = v; return this; }
        public Builder razorpaySignature(String v) { this.razorpaySignature = v; return this; }
        public Builder amount(BigDecimal v)         { this.amount = v; return this; }
        public Builder currency(String v)           { this.currency = v; return this; }
        public Builder description(String v)        { this.description = v; return this; }
        public Builder status(PaymentStatus v)      { this.status = v; return this; }
        public Builder user(User v)                 { this.user = v; return this; }
        public Payment build()                      { return new Payment(this); }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId()                            { return id; }
    public String getRazorpayOrderId()             { return razorpayOrderId; }
    public void setRazorpayOrderId(String v)       { this.razorpayOrderId = v; }
    public String getRazorpayPaymentId()           { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String v)     { this.razorpayPaymentId = v; }
    public String getRazorpaySignature()           { return razorpaySignature; }
    public void setRazorpaySignature(String v)     { this.razorpaySignature = v; }
    public BigDecimal getAmount()                  { return amount; }
    public void setAmount(BigDecimal v)            { this.amount = v; }
    public String getCurrency()                    { return currency; }
    public void setCurrency(String v)              { this.currency = v; }
    public String getDescription()                 { return description; }
    public void setDescription(String v)           { this.description = v; }
    public PaymentStatus getStatus()               { return status; }
    public void setStatus(PaymentStatus v)         { this.status = v; }
    public User getUser()                          { return user; }
    public void setUser(User v)                    { this.user = v; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public LocalDateTime getUpdatedAt()            { return updatedAt; }
}
