package com.finance.dashboard.dto.response;
import java.math.BigDecimal;
import java.util.List;
public class MonthlyTrendResponse {
    private List<MonthlyEntry> trends;
    private MonthlyTrendResponse(Builder b){this.trends=b.trends;}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private List<MonthlyEntry> trends;
        public Builder trends(List<MonthlyEntry> v){this.trends=v;return this;} public MonthlyTrendResponse build(){return new MonthlyTrendResponse(this);}
    }
    public List<MonthlyEntry> getTrends(){return trends;}
    public static class MonthlyEntry {
        private int year,month; private String monthLabel; private BigDecimal income,expenses,net;
        private MonthlyEntry(Builder b){this.year=b.year;this.month=b.month;this.monthLabel=b.monthLabel;this.income=b.income;this.expenses=b.expenses;this.net=b.net;}
        public static Builder builder(){return new Builder();}
        public static class Builder {
            private int year,month; private String monthLabel; private BigDecimal income=BigDecimal.ZERO,expenses=BigDecimal.ZERO,net=BigDecimal.ZERO;
            public Builder year(int v){this.year=v;return this;} public Builder month(int v){this.month=v;return this;}
            public Builder monthLabel(String v){this.monthLabel=v;return this;} public Builder income(BigDecimal v){this.income=v;return this;}
            public Builder expenses(BigDecimal v){this.expenses=v;return this;} public Builder net(BigDecimal v){this.net=v;return this;}
            public MonthlyEntry build(){return new MonthlyEntry(this);}
        }
        public int getYear(){return year;} public int getMonth(){return month;} public String getMonthLabel(){return monthLabel;}
        public BigDecimal getIncome(){return income;} public void setIncome(BigDecimal v){this.income=v;}
        public BigDecimal getExpenses(){return expenses;} public void setExpenses(BigDecimal v){this.expenses=v;}
        public BigDecimal getNet(){return net;} public void setNet(BigDecimal v){this.net=v;}
    }
}
