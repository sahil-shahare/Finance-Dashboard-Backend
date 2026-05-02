package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class TransactionResponse {
    private Long id; private BigDecimal amount; private TransactionType type; private String category; private LocalDate date; private String notes; private String createdBy; private LocalDateTime createdAt,updatedAt;
    private TransactionResponse(Builder b){this.id=b.id;this.amount=b.amount;this.type=b.type;this.category=b.category;this.date=b.date;this.notes=b.notes;this.createdBy=b.createdBy;this.createdAt=b.createdAt;this.updatedAt=b.updatedAt;}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Long id; private BigDecimal amount; private TransactionType type; private String category; private LocalDate date; private String notes; private String createdBy; private LocalDateTime createdAt,updatedAt;
        public Builder id(Long v){this.id=v;return this;} public Builder amount(BigDecimal v){this.amount=v;return this;}
        public Builder type(TransactionType v){this.type=v;return this;} public Builder category(String v){this.category=v;return this;}
        public Builder date(LocalDate v){this.date=v;return this;} public Builder notes(String v){this.notes=v;return this;}
        public Builder createdBy(String v){this.createdBy=v;return this;} public Builder createdAt(LocalDateTime v){this.createdAt=v;return this;}
        public Builder updatedAt(LocalDateTime v){this.updatedAt=v;return this;} public TransactionResponse build(){return new TransactionResponse(this);}
    }
    public static TransactionResponse from(Transaction t){return TransactionResponse.builder().id(t.getId()).amount(t.getAmount()).type(t.getType()).category(t.getCategory()).date(t.getDate()).notes(t.getNotes()).createdBy(t.getCreatedBy().getUsername()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();}
    public Long getId(){return id;} public BigDecimal getAmount(){return amount;} public TransactionType getType(){return type;}
    public String getCategory(){return category;} public LocalDate getDate(){return date;} public String getNotes(){return notes;}
    public String getCreatedBy(){return createdBy;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
