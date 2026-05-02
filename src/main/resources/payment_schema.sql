-- =============================================================
--  Payment Service — MySQL Table + Seed Data
--  Run AFTER the main schema.sql (users and transactions must exist).
-- =============================================================

USE finance_dashboard;

DROP TABLE IF EXISTS payments;

CREATE TABLE payments (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reference_number    VARCHAR(50)     NOT NULL,
    amount              DECIMAL(15, 2)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'INR',
    payment_method      ENUM('CREDIT_CARD','DEBIT_CARD','UPI','NET_BANKING','CASH','RAZORPAY')
                        NOT NULL,
    status              ENUM('PENDING','COMPLETED','FAILED','REFUNDED')
                        NOT NULL DEFAULT 'PENDING',
    description         VARCHAR(255)    NULL,
    payer_name          VARCHAR(100)    NULL,
    payee_name          VARCHAR(100)    NULL,

    -- Razorpay-specific columns (NULL for manual payments)
    razorpay_order_id   VARCHAR(100)    NULL COMMENT 'Razorpay order_xxxxxxxxxx',
    razorpay_payment_id VARCHAR(100)    NULL COMMENT 'Razorpay pay_xxxxxxxxxx — filled after verify',
    razorpay_signature  VARCHAR(255)    NULL COMMENT 'HMAC-SHA256 signature stored after verification',

    transaction_id      BIGINT          NULL,
    failure_reason      VARCHAR(500)    NULL,
    completed_at        DATETIME        NULL,
    created_by          BIGINT          NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_payments_reference       (reference_number),
    UNIQUE KEY uq_payments_rzp_order_id    (razorpay_order_id),
    UNIQUE KEY uq_payments_rzp_payment_id  (razorpay_payment_id),
    INDEX idx_payment_status               (status),
    INDEX idx_payment_method               (payment_method),
    INDEX idx_payment_created_at           (created_at),

    CONSTRAINT fk_payments_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_payments_user
        FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================
-- SEED DATA
-- =============================================================

INSERT INTO payments
    (reference_number, amount, currency, payment_method, status,
     description, payer_name, payee_name,
     razorpay_order_id, razorpay_payment_id,
     transaction_id, failure_reason, completed_at, created_by)
VALUES

-- Manual COMPLETED payments
('PAY-20250101-000001', 85000.00,'INR','NET_BANKING','COMPLETED','Salary credit - January',  'Employer Ltd','Sahil Shahare', NULL, NULL, 1, NULL,'2025-01-01 10:00:00',1),
('PAY-20250105-000002', 18500.00,'INR','NET_BANKING','COMPLETED','Office rent - January',    'Sahil Shahare','Property Owner',NULL,NULL,3,NULL,'2025-01-05 09:00:00',1),
('PAY-20250110-000003', 12000.00,'INR','UPI',        'COMPLETED','Freelance received',       'Client A','Sahil Shahare',    NULL,NULL,2,NULL,'2025-01-10 14:30:00',1),

-- Razorpay COMPLETED payments (simulated — use real IDs in production)
('RZP-20250620-001001', 35000.00,'INR','RAZORPAY',  'COMPLETED','Summer vacation booking',  NULL, NULL,
 'order_sim620A001','pay_sim620A001', NULL, NULL, '2025-06-20 15:00:00', 1),
('RZP-20251215-001002', 20000.00,'INR','RAZORPAY',  'COMPLETED','Year-end bonus transfer',  NULL, NULL,
 'order_sim1215B002','pay_sim1215B002', NULL, NULL, '2025-12-15 10:00:00', 1),

-- Razorpay PENDING (order created, payment not yet made)
('RZP-20260101-001003', 85000.00,'INR','RAZORPAY',  'PENDING',  'Salary - Jan 2026',        NULL, NULL,
 'order_sim0101C003', NULL, NULL, NULL, NULL, 1),

-- Manual PENDING
('PAY-20260105-000012', 18500.00,'INR','NET_BANKING','PENDING',  'Office rent - Jan 2026',  'Sahil Shahare','Property Owner',NULL,NULL,NULL,NULL,NULL,1),

-- FAILED
('PAY-20250918-000014', 22000.00,'INR','CREDIT_CARD','FAILED','Electronics purchase',       'Sahil Shahare','Electronics Hub',NULL,NULL,NULL,'Card declined - limit exceeded',NULL,1),
('RZP-20251105-001004', 8000.00, 'INR','RAZORPAY',  'FAILED','Transfer to savings',         NULL,NULL,
 'order_sim1105D004',NULL,NULL,'Payment abandoned by user',NULL,1),

-- REFUNDED
('RZP-20250625-001005', 12000.00,'INR','RAZORPAY',  'REFUNDED', 'Flight ticket - cancelled', NULL, NULL,
 'order_sim625E005','pay_sim625E005', NULL, 'Flight cancelled by airline', '2025-06-25 10:00:00', 1);
