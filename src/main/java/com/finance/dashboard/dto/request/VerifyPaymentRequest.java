package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload the frontend sends after the Razorpay checkout dialog completes.
 * The three fields are provided by the Razorpay SDK in the payment handler callback.
 */
public class VerifyPaymentRequest {

    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;

    public String getRazorpayOrderId()              { return razorpayOrderId; }
    public void setRazorpayOrderId(String v)        { this.razorpayOrderId = v; }
    public String getRazorpayPaymentId()            { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String v)      { this.razorpayPaymentId = v; }
    public String getRazorpaySignature()            { return razorpaySignature; }
    public void setRazorpaySignature(String v)      { this.razorpaySignature = v; }
}
