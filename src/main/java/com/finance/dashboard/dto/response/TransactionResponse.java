package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private TransactionResponse(Builder b) {
        this.id        = b.id;
        this.amount    = b.amount;
        this.type      = b.type;
        this.category  = b.category;
        this.date      = b.date;
        this.notes     = b.notes;
        this.createdBy = b.createdBy;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private LocalDate date;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        public Builder id(Long id)                    { this.id = id; return this; }
        public Builder amount(BigDecimal a)            { this.amount = a; return this; }
        public Builder type(TransactionType t)         { this.type = t; return this; }
        public Builder category(String c)              { this.category = c; return this; }
        public Builder date(LocalDate d)               { this.date = d; return this; }
        public Builder notes(String n)                 { this.notes = n; return this; }
        public Builder createdBy(String u)             { this.createdBy = u; return this; }
        public Builder createdAt(LocalDateTime c)      { this.createdAt = c; return this; }
        public Builder updatedAt(LocalDateTime u)      { this.updatedAt = u; return this; }
        public TransactionResponse build()             { return new TransactionResponse(this); }
    }

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .date(t.getDate())
                .notes(t.getNotes())
                .createdBy(t.getCreatedBy().getUsername())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    public Long getId()              { return id; }
    public BigDecimal getAmount()    { return amount; }
    public TransactionType getType() { return type; }
    public String getCategory()      { return category; }
    public LocalDate getDate()       { return date; }
    public String getNotes()         { return notes; }
    public String getCreatedBy()     { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
