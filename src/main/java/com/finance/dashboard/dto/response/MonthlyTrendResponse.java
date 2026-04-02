package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class MonthlyTrendResponse {

    private List<MonthlyEntry> trends;

    private MonthlyTrendResponse(Builder b) { this.trends = b.trends; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<MonthlyEntry> trends;
        public Builder trends(List<MonthlyEntry> t) { this.trends = t; return this; }
        public MonthlyTrendResponse build()          { return new MonthlyTrendResponse(this); }
    }

    public List<MonthlyEntry> getTrends() { return trends; }

    // ── Inner DTO ────────────────────────────────────────────────────────────

    public static class MonthlyEntry {
        private int year;
        private int month;
        private String monthLabel;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;

        private MonthlyEntry(Builder b) {
            this.year       = b.year;
            this.month      = b.month;
            this.monthLabel = b.monthLabel;
            this.income     = b.income;
            this.expenses   = b.expenses;
            this.net        = b.net;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private int year;
            private int month;
            private String monthLabel;
            private BigDecimal income   = BigDecimal.ZERO;
            private BigDecimal expenses = BigDecimal.ZERO;
            private BigDecimal net      = BigDecimal.ZERO;
            public Builder year(int y)           { this.year = y; return this; }
            public Builder month(int m)          { this.month = m; return this; }
            public Builder monthLabel(String l)  { this.monthLabel = l; return this; }
            public Builder income(BigDecimal v)  { this.income = v; return this; }
            public Builder expenses(BigDecimal v){ this.expenses = v; return this; }
            public Builder net(BigDecimal v)     { this.net = v; return this; }
            public MonthlyEntry build()          { return new MonthlyEntry(this); }
        }

        public int getYear()             { return year; }
        public int getMonth()            { return month; }
        public String getMonthLabel()    { return monthLabel; }
        public BigDecimal getIncome()    { return income; }
        public void setIncome(BigDecimal income)     { this.income = income; }
        public BigDecimal getExpenses()  { return expenses; }
        public void setExpenses(BigDecimal expenses) { this.expenses = expenses; }
        public BigDecimal getNet()       { return net; }
        public void setNet(BigDecimal net)           { this.net = net; }
    }
}
