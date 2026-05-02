package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.NotNull;

public class ProcessPaymentRequest {

    @NotNull(message = "success flag is required")
    private Boolean success;

    private String failureReason;

    public Boolean getSuccess()                  { return success; }
    public void setSuccess(Boolean s)            { this.success = s; }
    public String getFailureReason()             { return failureReason; }
    public void setFailureReason(String r)       { this.failureReason = r; }
}
