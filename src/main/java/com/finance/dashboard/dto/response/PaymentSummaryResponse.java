package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PaymentSummaryResponse {

    private long totalPayments;
    private long pendingCount;
    private long completedCount;
    private long failedCount;
    private long refundedCount;
    private BigDecimal totalCompleted;
    private BigDecimal totalPending;
    private BigDecimal totalRefunded;
    private List<MethodBreakdown> methodBreakdown;
    private List<PaymentResponse> recentPayments;

    private PaymentSummaryResponse(Builder b) {
        this.totalPayments   = b.totalPayments;
        this.pendingCount    = b.pendingCount;
        this.completedCount  = b.completedCount;
        this.failedCount     = b.failedCount;
        this.refundedCount   = b.refundedCount;
        this.totalCompleted  = b.totalCompleted;
        this.totalPending    = b.totalPending;
        this.totalRefunded   = b.totalRefunded;
        this.methodBreakdown = b.methodBreakdown;
        this.recentPayments  = b.recentPayments;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalPayments;
        private long pendingCount;
        private long completedCount;
        private long failedCount;
        private long refundedCount;
        private BigDecimal totalCompleted;
        private BigDecimal totalPending;
        private BigDecimal totalRefunded;
        private List<MethodBreakdown> methodBreakdown;
        private List<PaymentResponse> recentPayments;

        public Builder totalPayments(long v)                      { this.totalPayments = v; return this; }
        public Builder pendingCount(long v)                       { this.pendingCount = v; return this; }
        public Builder completedCount(long v)                     { this.completedCount = v; return this; }
        public Builder failedCount(long v)                        { this.failedCount = v; return this; }
        public Builder refundedCount(long v)                      { this.refundedCount = v; return this; }
        public Builder totalCompleted(BigDecimal v)               { this.totalCompleted = v; return this; }
        public Builder totalPending(BigDecimal v)                 { this.totalPending = v; return this; }
        public Builder totalRefunded(BigDecimal v)                { this.totalRefunded = v; return this; }
        public Builder methodBreakdown(List<MethodBreakdown> v)   { this.methodBreakdown = v; return this; }
        public Builder recentPayments(List<PaymentResponse> v)    { this.recentPayments = v; return this; }
        public PaymentSummaryResponse build()                     { return new PaymentSummaryResponse(this); }
    }

    public long getTotalPayments()                      { return totalPayments; }
    public long getPendingCount()                       { return pendingCount; }
    public long getCompletedCount()                     { return completedCount; }
    public long getFailedCount()                        { return failedCount; }
    public long getRefundedCount()                      { return refundedCount; }
    public BigDecimal getTotalCompleted()               { return totalCompleted; }
    public BigDecimal getTotalPending()                 { return totalPending; }
    public BigDecimal getTotalRefunded()                { return totalRefunded; }
    public List<MethodBreakdown> getMethodBreakdown()   { return methodBreakdown; }
    public List<PaymentResponse> getRecentPayments()    { return recentPayments; }

    // ── Inner DTO ────────────────────────────────────────────────────────────

    public static class MethodBreakdown {
        private String method;
        private String status;
        private BigDecimal total;

        private MethodBreakdown(Builder b) {
            this.method = b.method;
            this.status = b.status;
            this.total  = b.total;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String method;
            private String status;
            private BigDecimal total;
            public Builder method(String m)    { this.method = m; return this; }
            public Builder status(String s)    { this.status = s; return this; }
            public Builder total(BigDecimal t) { this.total = t; return this; }
            public MethodBreakdown build()     { return new MethodBreakdown(this); }
        }

        public String getMethod()        { return method; }
        public String getStatus()        { return status; }
        public BigDecimal getTotal()     { return total; }
    }
}
