package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/payments/razorpay/verify.
 *
 * After the user completes payment on the Razorpay checkout widget,
 * Razorpay returns three values to the frontend handler:
 *   razorpay_order_id   — the order ID we created
 *   razorpay_payment_id — the payment ID assigned by Razorpay
 *   razorpay_signature  — HMAC-SHA256 signature for verification
 *
 * The frontend forwards all three here and the backend verifies the
 * signature before marking the payment as COMPLETED.
 */
public class RazorpayVerifyRequest {

    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpaySignature is required")
    private String razorpaySignature;

    public String getRazorpayOrderId()              { return razorpayOrderId; }
    public void setRazorpayOrderId(String r)        { this.razorpayOrderId = r; }
    public String getRazorpayPaymentId()            { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String r)      { this.razorpayPaymentId = r; }
    public String getRazorpaySignature()            { return razorpaySignature; }
    public void setRazorpaySignature(String r)      { this.razorpaySignature = r; }
}
