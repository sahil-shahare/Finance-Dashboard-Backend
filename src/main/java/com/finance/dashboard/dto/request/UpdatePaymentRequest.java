package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.enums.PaymentMethod;
import jakarta.validation.constraints.Size;

public class UpdatePaymentRequest {

    private PaymentMethod paymentMethod;

    @Size(max = 255) private String description;
    @Size(max = 100) private String payerName;
    @Size(max = 100) private String payeeName;

    public PaymentMethod getPaymentMethod()      { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m){ this.paymentMethod = m; }
    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }
    public String getPayerName()                 { return payerName; }
    public void setPayerName(String p)           { this.payerName = p; }
    public String getPayeeName()                 { return payeeName; }
    public void setPayeeName(String p)           { this.payeeName = p; }
}
