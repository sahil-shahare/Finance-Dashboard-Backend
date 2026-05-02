package com.finance.dashboard.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initialises the Razorpay SDK client as a singleton Spring bean.
 *
 * Keys are injected from application.properties — never hardcoded.
 * Use your TEST mode keys during development (prefix rzp_test_).
 * Switch to LIVE keys (prefix rzp_live_) only in production.
 */
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
