package com.finance.dashboard.dto.response;

import java.math.BigDecimal;

/**
 * Returned after successfully creating a Razorpay order.
 * The frontend passes these values directly into the Razorpay checkout SDK.
 */
public class PaymentOrderResponse {

    private String  razorpayOrderId;   // order_xxxxx
    private BigDecimal amount;         // original amount in ₹
    private int     amountInPaise;     // amount × 100, passed to Razorpay SDK
    private String  currency;
    private String  keyId;             // Razorpay publishable key — safe to expose to frontend
    private String  description;
    private String  status;

    private PaymentOrderResponse(Builder b) {
        this.razorpayOrderId = b.razorpayOrderId;
        this.amount          = b.amount;
        this.amountInPaise   = b.amountInPaise;
        this.currency        = b.currency;
        this.keyId           = b.keyId;
        this.description     = b.description;
        this.status          = b.status;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String razorpayOrderId;
        private BigDecimal amount;
        private int amountInPaise;
        private String currency;
        private String keyId;
        private String description;
        private String status;
        public Builder razorpayOrderId(String v)  { this.razorpayOrderId = v; return this; }
        public Builder amount(BigDecimal v)        { this.amount = v; return this; }
        public Builder amountInPaise(int v)        { this.amountInPaise = v; return this; }
        public Builder currency(String v)          { this.currency = v; return this; }
        public Builder keyId(String v)             { this.keyId = v; return this; }
        public Builder description(String v)       { this.description = v; return this; }
        public Builder status(String v)            { this.status = v; return this; }
        public PaymentOrderResponse build()        { return new PaymentOrderResponse(this); }
    }

    public String getRazorpayOrderId()  { return razorpayOrderId; }
    public BigDecimal getAmount()       { return amount; }
    public int getAmountInPaise()       { return amountInPaise; }
    public String getCurrency()         { return currency; }
    public String getKeyId()            { return keyId; }
    public String getDescription()      { return description; }
    public String getStatus()           { return status; }
}
