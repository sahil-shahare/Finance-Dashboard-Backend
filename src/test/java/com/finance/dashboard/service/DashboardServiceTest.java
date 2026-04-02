package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.MonthlyTrendResponse;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.model.enums.UserStatus;
import com.finance.dashboard.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private DashboardService dashboardService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).username("admin").email("admin@test.com")
                .password("hashed").role(Role.ADMIN).status(UserStatus.ACTIVE)
                .build();
    }

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSummary: calculates net balance as income minus expenses")
    void getSummary_netBalance() {
        when(transactionRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("1200.00"));
        when(transactionRepository.sumByCategory()).thenReturn(List.of());
        when(transactionRepository.findRecentTransactions(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("3000.00");
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo("1200.00");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("1800.00");
    }

    @Test
    @DisplayName("getSummary: includes recent transactions in response")
    void getSummary_includesRecentTransactions() {
        Transaction t = Transaction.builder()
                .id(1L).amount(new BigDecimal("100")).type(TransactionType.INCOME)
                .category("Bonus").date(LocalDate.now()).createdBy(adminUser).deleted(false)
                .build();

        when(transactionRepository.sumByType(TransactionType.INCOME)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByCategory()).thenReturn(List.of());
        when(transactionRepository.findRecentTransactions(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t)));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getRecentTransactions()).hasSize(1);
        assertThat(summary.getRecentTransactions().get(0).getCategory()).isEqualTo("Bonus");
    }

    @Test
    @DisplayName("getSummary: net balance is negative when expenses exceed income")
    void getSummary_negativeNetBalance() {
        when(transactionRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("800.00"));
        when(transactionRepository.sumByCategory()).thenReturn(List.of());
        when(transactionRepository.findRecentTransactions(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getNetBalance()).isNegative();
        assertThat(summary.getNetBalance()).isEqualByComparingTo("-300.00");
    }

    // ── getMonthlyTrends ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getMonthlyTrends: merges income and expense rows for the same month")
    void getMonthlyTrends_mergesRows() {
        // Each row: [year, month, type (String), amount]
        List<Object[]> rows = List.of(
                new Object[]{2025, 1, "INCOME",  new BigDecimal("2000.00")},
                new Object[]{2025, 1, "EXPENSE", new BigDecimal("800.00")},
                new Object[]{2025, 2, "INCOME",  new BigDecimal("2100.00")}
        );

        when(transactionRepository.monthlyTrends(any(LocalDate.class))).thenReturn(rows);

        MonthlyTrendResponse response = dashboardService.getMonthlyTrends();

        assertThat(response.getTrends()).hasSize(2);

        MonthlyTrendResponse.MonthlyEntry jan = response.getTrends().get(0);
        assertThat(jan.getIncome()).isEqualByComparingTo("2000.00");
        assertThat(jan.getExpenses()).isEqualByComparingTo("800.00");
        assertThat(jan.getNet()).isEqualByComparingTo("1200.00");

        MonthlyTrendResponse.MonthlyEntry feb = response.getTrends().get(1);
        assertThat(feb.getIncome()).isEqualByComparingTo("2100.00");
        assertThat(feb.getExpenses()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("getMonthlyTrends: returns empty list when no data exists")
    void getMonthlyTrends_empty() {
        when(transactionRepository.monthlyTrends(any(LocalDate.class))).thenReturn(List.of());

        MonthlyTrendResponse response = dashboardService.getMonthlyTrends();

        assertThat(response.getTrends()).isEmpty();
    }
}
