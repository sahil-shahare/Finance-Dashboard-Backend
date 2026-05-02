

-- =============================================================
-- 5. PAYMENTS TABLE  (added with Razorpay integration)
-- =============================================================

DROP TABLE IF EXISTS payments;

CREATE TABLE payments (
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    razorpay_order_id    VARCHAR(100)   NOT NULL,
    razorpay_payment_id  VARCHAR(100)   NULL,
    razorpay_signature   VARCHAR(255)   NULL,
    amount               DECIMAL(15,2)  NOT NULL,
    currency             VARCHAR(10)    NOT NULL DEFAULT 'INR',
    description          VARCHAR(255)   NULL,
    status               ENUM('CREATED','SUCCESS','FAILED','REFUNDED') NOT NULL,
    user_id              BIGINT         NOT NULL,
    created_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_razorpay_order_id (razorpay_order_id),

    CONSTRAINT fk_payments_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    INDEX idx_payment_razorpay_order   (razorpay_order_id),
    INDEX idx_payment_razorpay_payment (razorpay_payment_id),
    INDEX idx_payment_status           (status),
    INDEX idx_payment_user             (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample payment records (TEST mode — no real money)
INSERT INTO payments (razorpay_order_id, razorpay_payment_id, razorpay_signature, amount, currency, description, status, user_id) VALUES
('order_test_001', 'pay_test_001', 'sig_test_001', 499.00,  'INR', 'Monthly Premium Plan',    'SUCCESS',  1),
('order_test_002', 'pay_test_002', 'sig_test_002', 999.00,  'INR', 'Annual Subscription',      'SUCCESS',  2),
('order_test_003', NULL,           NULL,           199.00,  'INR', 'One-time Report Export',   'CREATED',  1),
('order_test_004', 'pay_test_004', 'sig_test_004', 1499.00, 'INR', 'Enterprise Add-on',        'FAILED',   3);
