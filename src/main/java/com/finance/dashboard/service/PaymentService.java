package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.request.CreateOrderRequest;
import com.finance.dashboard.dto.request.VerifyPaymentRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.PaymentOrderResponse;
import com.finance.dashboard.dto.response.PaymentResponse;
import com.finance.dashboard.exception.PaymentException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Payment;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.PaymentStatus;
import com.finance.dashboard.repository.PaymentRepository;
import com.finance.dashboard.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final RazorpayClient    razorpayClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository    userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(RazorpayClient razorpayClient,
                          PaymentRepository paymentRepository,
                          UserRepository userRepository) {
        this.razorpayClient    = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.userRepository    = userRepository;
    }

    // ── Create Order ─────────────────────────────────────────────────────────

    @Transactional
    public PaymentOrderResponse createOrder(CreateOrderRequest request, String username) {
        User user = loadUser(username);
        int amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt",  "rcpt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = Payment.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .description(request.getDescription())
                    .status(PaymentStatus.CREATED)
                    .user(user)
                    .build();

            paymentRepository.save(payment);
            log.info("Created Razorpay order {} for user {} — ₹{}",
                    razorpayOrderId, username, request.getAmount());

            return PaymentOrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .amountInPaise(amountInPaise)
                    .currency(request.getCurrency())
                    .keyId(razorpayKeyId)
                    .description(request.getDescription())
                    .status("CREATED")
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        }
    }

    // ── Verify Payment ────────────────────────────────────────────────────────

    /**
     * After successful verification, @CachePut stores the payment in Redis
     * so the next GET /api/payments/{id} is served from cache.
     */
    @Transactional
    @CachePut(value = CacheConstants.PAYMENT_BY_ID, key = "#result.id")
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + request.getRazorpayOrderId()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentException("Payment already verified for order: "
                    + request.getRazorpayOrderId());
        }

        boolean valid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if (valid) {
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment verified — order={} pay={}",
                    request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);   // persist FAILED status for audit trail
            log.warn("Signature FAILED for order={}", request.getRazorpayOrderId());
            throw new PaymentException("Signature verification failed. Possible tamper attempt.");
        }

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Cached by payment ID — payment records don't change after SUCCESS/FAILED.
     */
    @Cacheable(value = CacheConstants.PAYMENT_BY_ID, key = "#id")
    public PaymentResponse getPaymentById(Long id) {
        return PaymentResponse.from(
                paymentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Payment not found: " + id)));
    }

    public PagedResponse<PaymentResponse> getMyPayments(String username, int page, int size) {
        User user = loadUser(username);
        return PagedResponse.from(
                paymentRepository.findByUserId(user.getId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                PaymentResponse::from);
    }

    public PagedResponse<PaymentResponse> getAllPayments(int page, int size) {
        return PagedResponse.from(
                paymentRepository.findAllRecent(PageRequest.of(page, size)),
                PaymentResponse::from);
    }

    public BigDecimal getTotalRevenue() {
        return paymentRepository.sumByStatus(PaymentStatus.SUCCESS);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
