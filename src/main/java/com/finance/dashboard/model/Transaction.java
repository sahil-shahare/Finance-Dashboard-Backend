package com.finance.dashboard.model;

import com.finance.dashboard.model.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_type",     columnList = "type"),
        @Index(name = "idx_transaction_date",     columnList = "date"),
        @Index(name = "idx_transaction_category", columnList = "category")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Transaction() {}

    private Transaction(Builder b) {
        this.id        = b.id;
        this.amount    = b.amount;
        this.type      = b.type;
        this.category  = b.category;
        this.date      = b.date;
        this.notes     = b.notes;
        this.deleted   = b.deleted;
        this.createdBy = b.createdBy;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private LocalDate date;
        private String notes;
        private boolean deleted = false;
        private User createdBy;
        public Builder id(Long id)                   { this.id = id; return this; }
        public Builder amount(BigDecimal a)           { this.amount = a; return this; }
        public Builder type(TransactionType t)        { this.type = t; return this; }
        public Builder category(String c)             { this.category = c; return this; }
        public Builder date(LocalDate d)              { this.date = d; return this; }
        public Builder notes(String n)                { this.notes = n; return this; }
        public Builder deleted(boolean d)             { this.deleted = d; return this; }
        public Builder createdBy(User u)              { this.createdBy = u; return this; }
        public Transaction build()                    { return new Transaction(this); }
    }

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }
    public BigDecimal getAmount()                 { return amount; }
    public void setAmount(BigDecimal amount)      { this.amount = amount; }
    public TransactionType getType()              { return type; }
    public void setType(TransactionType type)     { this.type = type; }
    public String getCategory()                   { return category; }
    public void setCategory(String category)      { this.category = category; }
    public LocalDate getDate()                    { return date; }
    public void setDate(LocalDate date)           { this.date = date; }
    public String getNotes()                      { return notes; }
    public void setNotes(String notes)            { this.notes = notes; }
    public boolean isDeleted()                    { return deleted; }
    public void setDeleted(boolean deleted)       { this.deleted = deleted; }
    public User getCreatedBy()                    { return createdBy; }
    public void setCreatedBy(User createdBy)      { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
}
