package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request body for POST /api/payments/razorpay/create-order.
 *
 * amount     — in your currency unit (e.g. 499.00 for ₹499).
 *              Razorpay works in paise internally; the service handles conversion.
 * currency   — ISO 4217 code. Defaults to "INR" if omitted.
 * receipt    — short client-side identifier (invoice number, order ID, etc.)
 * description — shown to the user on the Razorpay checkout screen.
 */
public class RazorpayOrderRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is ₹1.00")
    @Digits(integer = 11, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "INR";

    @NotBlank(message = "Receipt / reference is required")
    @Size(max = 40, message = "Receipt must be 40 characters or fewer")
    private String receipt;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /** Optional: link this Razorpay order to an existing transaction */
    private Long transactionId;

    public BigDecimal getAmount()           { return amount; }
    public void setAmount(BigDecimal a)     { this.amount = a; }
    public String getCurrency()             { return currency; }
    public void setCurrency(String c)       { this.currency = c; }
    public String getReceipt()              { return receipt; }
    public void setReceipt(String r)        { this.receipt = r; }
    public String getDescription()          { return description; }
    public void setDescription(String d)    { this.description = d; }
    public Long getTransactionId()          { return transactionId; }
    public void setTransactionId(Long t)    { this.transactionId = t; }
}
