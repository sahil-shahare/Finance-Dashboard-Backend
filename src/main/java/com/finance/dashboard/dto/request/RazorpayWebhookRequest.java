package com.finance.dashboard.dto.request;

/**
 * Raw webhook payload from Razorpay.
 * We only care about the event type and the payment / order IDs.
 * The full payload is passed as a String so we can verify the
 * X-Razorpay-Signature header before deserialising.
 */
public class RazorpayWebhookRequest {

    private String event;
    private Object payload;

    public String getEvent()          { return event; }
    public void setEvent(String e)    { this.event = e; }
    public Object getPayload()        { return payload; }
    public void setPayload(Object p)  { this.payload = p; }
}
