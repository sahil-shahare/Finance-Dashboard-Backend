package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public class UpdateTransactionRequest {
    @DecimalMin("0.01") @Digits(integer=13,fraction=2) private BigDecimal amount;
    private TransactionType type;
    @Size(max=100) private String category;
    private LocalDate date;
    @Size(max=500) private String notes;
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){this.amount=v;}
    public TransactionType getType(){return type;} public void setType(TransactionType v){this.type=v;}
    public String getCategory(){return category;} public void setCategory(String v){this.category=v;}
    public LocalDate getDate(){return date;} public void setDate(LocalDate v){this.date=v;}
    public String getNotes(){return notes;} public void setNotes(String v){this.notes=v;}
}
