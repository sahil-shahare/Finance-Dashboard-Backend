package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateOrderRequest {

    /**
     * Amount in the primary currency unit (e.g. ₹100.00).
     * The service converts this to paise before calling Razorpay (x100).
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is ₹1.00")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 3, message = "Currency code must be 3 characters (e.g. INR)")
    private String currency = "INR";

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public BigDecimal getAmount()          { return amount; }
    public void setAmount(BigDecimal v)    { this.amount = v; }
    public String getCurrency()            { return currency; }
    public void setCurrency(String v)      { this.currency = v != null ? v : "INR"; }
    public String getDescription()         { return description; }
    public void setDescription(String v)   { this.description = v; }
}
