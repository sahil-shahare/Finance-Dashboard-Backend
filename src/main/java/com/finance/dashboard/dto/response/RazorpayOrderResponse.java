package com.finance.dashboard.dto.response;

import java.math.BigDecimal;

/**
 * Returned to the frontend after POST /api/payments/razorpay/create-order.
 *
 * The frontend uses these fields to initialise the Razorpay checkout widget:
 *
 *   var options = {
 *     key:         keyId,
 *     amount:      amountInPaise,
 *     currency:    currency,
 *     name:        "Finance Dashboard",
 *     description: description,
 *     order_id:    razorpayOrderId,
 *     handler: function(response) {
 *       // POST /api/payments/razorpay/verify with the three values
 *     }
 *   };
 *   var rzp = new Razorpay(options);
 *   rzp.open();
 */
public class RazorpayOrderResponse {

    private Long paymentId;           // our internal Payment.id
    private String referenceNumber;   // our PAY-YYYYMMDD-XXXXXX
    private String razorpayOrderId;   // Razorpay order_xxxxxxxxxx
    private String keyId;             // Razorpay key ID (safe to expose — NOT the secret)
    private BigDecimal amount;        // in currency units (e.g. ₹499.00)
    private long amountInPaise;       // in smallest currency unit for Razorpay SDK
    private String currency;
    private String description;
    private String status;

    private RazorpayOrderResponse(Builder b) {
        this.paymentId       = b.paymentId;
        this.referenceNumber = b.referenceNumber;
        this.razorpayOrderId = b.razorpayOrderId;
        this.keyId           = b.keyId;
        this.amount          = b.amount;
        this.amountInPaise   = b.amountInPaise;
        this.currency        = b.currency;
        this.description     = b.description;
        this.status          = b.status;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long paymentId;
        private String referenceNumber;
        private String razorpayOrderId;
        private String keyId;
        private BigDecimal amount;
        private long amountInPaise;
        private String currency;
        private String description;
        private String status;

        public Builder paymentId(Long v)           { this.paymentId = v; return this; }
        public Builder referenceNumber(String v)   { this.referenceNumber = v; return this; }
        public Builder razorpayOrderId(String v)   { this.razorpayOrderId = v; return this; }
        public Builder keyId(String v)             { this.keyId = v; return this; }
        public Builder amount(BigDecimal v)        { this.amount = v; return this; }
        public Builder amountInPaise(long v)       { this.amountInPaise = v; return this; }
        public Builder currency(String v)          { this.currency = v; return this; }
        public Builder description(String v)       { this.description = v; return this; }
        public Builder status(String v)            { this.status = v; return this; }
        public RazorpayOrderResponse build()       { return new RazorpayOrderResponse(this); }
    }

    public Long getPaymentId()          { return paymentId; }
    public String getReferenceNumber()  { return referenceNumber; }
    public String getRazorpayOrderId()  { return razorpayOrderId; }
    public String getKeyId()            { return keyId; }
    public BigDecimal getAmount()       { return amount; }
    public long getAmountInPaise()      { return amountInPaise; }
    public String getCurrency()         { return currency; }
    public String getDescription()      { return description; }
    public String getStatus()           { return status; }
}
