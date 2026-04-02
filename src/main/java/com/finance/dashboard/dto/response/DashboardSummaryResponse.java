package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<CategoryTotalResponse> categoryTotals;
    private List<TransactionResponse> recentTransactions;

    private DashboardSummaryResponse(Builder b) {
        this.totalIncome         = b.totalIncome;
        this.totalExpenses       = b.totalExpenses;
        this.netBalance          = b.netBalance;
        this.categoryTotals      = b.categoryTotals;
        this.recentTransactions  = b.recentTransactions;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
        private List<CategoryTotalResponse> categoryTotals;
        private List<TransactionResponse> recentTransactions;
        public Builder totalIncome(BigDecimal v)                          { this.totalIncome = v; return this; }
        public Builder totalExpenses(BigDecimal v)                        { this.totalExpenses = v; return this; }
        public Builder netBalance(BigDecimal v)                           { this.netBalance = v; return this; }
        public Builder categoryTotals(List<CategoryTotalResponse> v)      { this.categoryTotals = v; return this; }
        public Builder recentTransactions(List<TransactionResponse> v)    { this.recentTransactions = v; return this; }
        public DashboardSummaryResponse build()                           { return new DashboardSummaryResponse(this); }
    }

    public BigDecimal getTotalIncome()                          { return totalIncome; }
    public BigDecimal getTotalExpenses()                        { return totalExpenses; }
    public BigDecimal getNetBalance()                           { return netBalance; }
    public List<CategoryTotalResponse> getCategoryTotals()      { return categoryTotals; }
    public List<TransactionResponse> getRecentTransactions()    { return recentTransactions; }

    // ── Inner DTO ────────────────────────────────────────────────────────────

    public static class CategoryTotalResponse {
        private String category;
        private String type;
        private BigDecimal total;

        private CategoryTotalResponse(Builder b) {
            this.category = b.category;
            this.type     = b.type;
            this.total    = b.total;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String category;
            private String type;
            private BigDecimal total;
            public Builder category(String c)  { this.category = c; return this; }
            public Builder type(String t)      { this.type = t; return this; }
            public Builder total(BigDecimal t) { this.total = t; return this; }
            public CategoryTotalResponse build() { return new CategoryTotalResponse(this); }
        }

        public String getCategory()   { return category; }
        public String getType()       { return type; }
        public BigDecimal getTotal()  { return total; }
    }
}
