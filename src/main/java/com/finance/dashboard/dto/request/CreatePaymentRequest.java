package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 100) private String payerName;
    @Size(max = 100) private String payeeName;
    private Long transactionId;

    public BigDecimal getAmount()               { return amount; }
    public void setAmount(BigDecimal a)         { this.amount = a; }
    public PaymentMethod getPaymentMethod()     { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m){ this.paymentMethod = m; }
    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }
    public String getPayerName()                { return payerName; }
    public void setPayerName(String p)          { this.payerName = p; }
    public String getPayeeName()                { return payeeName; }
    public void setPayeeName(String p)          { this.payeeName = p; }
    public Long getTransactionId()              { return transactionId; }
    public void setTransactionId(Long t)        { this.transactionId = t; }
}
