package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.CreateOrderRequest;
import com.finance.dashboard.dto.request.VerifyPaymentRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.PaymentOrderResponse;
import com.finance.dashboard.dto.response.PaymentResponse;
import com.finance.dashboard.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/**
	 * POST /api/payments/create-order
	 *
	 * All authenticated users can initiate a payment. Returns the Razorpay order_id
	 * + key_id needed by the frontend checkout SDK.
	 *
	 * Frontend flow after this call: const rzp = new Razorpay({ key:
	 * response.data.keyId, order_id: response.data.razorpayOrderId, amount:
	 * response.data.amountInPaise, ... }); rzp.open();
	 */
	@PostMapping("/create-order")
	public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {

		PaymentOrderResponse order = paymentService.createOrder(request, userDetails.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Payment order created successfully", order));
	}

	/**
	 * POST /api/payments/verify
	 *
	 * Called by the frontend after the Razorpay checkout dialog completes. The
	 * three Razorpay-provided values are verified via HMAC-SHA256 signature check.
	 * On success, the payment record is updated to SUCCESS in our database.
	 */
	@PostMapping("/verify")
	public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
			@Valid @RequestBody VerifyPaymentRequest request) {

		PaymentResponse payment = paymentService.verifyPayment(request);
		return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
	}

	/**
	 * GET /api/payments/my
	 *
	 * Returns the authenticated user's own payment history (paginated). Any role
	 * can view their own payments.
	 */
	@GetMapping("/my")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getMyPayments(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal UserDetails userDetails) {

		return ResponseEntity
				.ok(ApiResponse.success(paymentService.getMyPayments(userDetails.getUsername(), page, size)));
	}

	/**
	 * GET /api/payments/{id}
	 *
	 * Fetch a single payment by its internal DB id. Any authenticated user can look
	 * up a payment (ADMIN may want to review specific ones).
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
	}

	/**
	 * GET /api/payments — ADMIN only
	 *
	 * Lists all payments across all users. Used for admin oversight and revenue
	 * reporting.
	 */
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getAllPayments(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPayments(page, size)));
	}

	/**
	 * GET /api/payments/revenue — ADMIN only
	 *
	 * Returns total revenue collected (sum of all SUCCESS payments).
	 */
	@GetMapping("/revenue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getTotalRevenue() {
		BigDecimal total = paymentService.getTotalRevenue();
		return ResponseEntity.ok(ApiResponse.success(Map.of("totalRevenue", total)));
	}
}
