package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Payment;
import com.finance.dashboard.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long          id;
    private String        razorpayOrderId;
    private String        razorpayPaymentId;
    private BigDecimal    amount;
    private String        currency;
    private String        description;
    private PaymentStatus status;
    private String        initiatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PaymentResponse(Builder b) {
        this.id                = b.id;
        this.razorpayOrderId   = b.razorpayOrderId;
        this.razorpayPaymentId = b.razorpayPaymentId;
        this.amount            = b.amount;
        this.currency          = b.currency;
        this.description       = b.description;
        this.status            = b.status;
        this.initiatedBy       = b.initiatedBy;
        this.createdAt         = b.createdAt;
        this.updatedAt         = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private BigDecimal amount;
        private String currency;
        private String description;
        private PaymentStatus status;
        private String initiatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long v)                    { this.id = v; return this; }
        public Builder razorpayOrderId(String v)     { this.razorpayOrderId = v; return this; }
        public Builder razorpayPaymentId(String v)   { this.razorpayPaymentId = v; return this; }
        public Builder amount(BigDecimal v)           { this.amount = v; return this; }
        public Builder currency(String v)             { this.currency = v; return this; }
        public Builder description(String v)          { this.description = v; return this; }
        public Builder status(PaymentStatus v)        { this.status = v; return this; }
        public Builder initiatedBy(String v)          { this.initiatedBy = v; return this; }
        public Builder createdAt(LocalDateTime v)     { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)     { this.updatedAt = v; return this; }
        public PaymentResponse build()                { return new PaymentResponse(this); }
    }

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .description(p.getDescription())
                .status(p.getStatus())
                .initiatedBy(p.getUser().getUsername())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public Long getId()                       { return id; }
    public String getRazorpayOrderId()        { return razorpayOrderId; }
    public String getRazorpayPaymentId()      { return razorpayPaymentId; }
    public BigDecimal getAmount()             { return amount; }
    public String getCurrency()               { return currency; }
    public String getDescription()            { return description; }
    public PaymentStatus getStatus()          { return status; }
    public String getInitiatedBy()            { return initiatedBy; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
}
