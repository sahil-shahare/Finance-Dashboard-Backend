package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse.CategoryTotalResponse;
import com.finance.dashboard.dto.response.MonthlyTrendResponse;
import com.finance.dashboard.dto.response.MonthlyTrendResponse.MonthlyEntry;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_LIMIT = 10;
    private static final int TREND_MONTHS = 12;

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Cacheable(value = CacheConstants.DASHBOARD_SUMMARY, key = "'global'")
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        List<TransactionResponse> recent = transactionRepository
                .findRecentTransactions(PageRequest.of(0, RECENT_LIMIT))
                .getContent()
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(buildCategoryTotals())
                .recentTransactions(recent)
                .build();
    }

    @Cacheable(value = CacheConstants.DASHBOARD_TRENDS, key = "'global'")
    public MonthlyTrendResponse getMonthlyTrends() {
        LocalDate since = LocalDate.now().minusMonths(TREND_MONTHS).withDayOfMonth(1);
        List<Object[]> rows = transactionRepository.monthlyTrends(since);

        Map<String, MonthlyEntry> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            String key = year + "-" + month;

            /**
             * FIX: ClassCastException on Hibernate 6 / Spring Boot 3.
             *
             * The JPQL query selects t.type which is mapped as
             * @Enumerated(EnumType.STRING). Hibernate 5 returned the raw
             * String name; Hibernate 6 returns the actual enum object.
             *
             * OLD (breaks on Hibernate 6):
             *   TransactionType type = TransactionType.valueOf((String) row[2]);
             *
             * NEW (works on both):
             *   row[2] is already a TransactionType — cast directly.
             *   If somehow a String arrives (edge case), handle gracefully.
             */
            TransactionType type;
            if (row[2] instanceof TransactionType) {
                type = (TransactionType) row[2];
            } else {
                // fallback for any edge case where a String arrives
                type = TransactionType.valueOf(row[2].toString());
            }

            BigDecimal amount = (BigDecimal) row[3];

            map.computeIfAbsent(key, k -> MonthlyEntry.builder()
                    .year(year).month(month)
                    .monthLabel(Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            + " " + year)
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            MonthlyEntry entry = map.get(key);
            if (type == TransactionType.INCOME) {
                entry.setIncome(amount);
            } else {
                entry.setExpenses(amount);
            }
            entry.setNet(entry.getIncome().subtract(entry.getExpenses()));
        }

        return MonthlyTrendResponse.builder()
                .trends(new ArrayList<>(map.values()))
                .build();
    }

    @Cacheable(value = CacheConstants.CATEGORY_TOTALS, key = "'global'")
    public List<CategoryTotalResponse> getCategoryTotals() {
        return buildCategoryTotals();
    }

    @CacheEvict(value = {
            CacheConstants.DASHBOARD_SUMMARY,
            CacheConstants.DASHBOARD_TRENDS,
            CacheConstants.CATEGORY_TOTALS
    }, allEntries = true)
    public void evictDashboardCaches() {
        // Spring AOP handles eviction — no body needed
    }

    private List<CategoryTotalResponse> buildCategoryTotals() {
        return transactionRepository.sumByCategory()
                .stream()
                .map(row -> {
                    /**
                     * FIX: same as above — row[1] is TransactionType on Hibernate 6,
                     * not a String. Use .name() to get the string representation.
                     */
                    String typeName = (row[1] instanceof TransactionType)
                            ? ((TransactionType) row[1]).name()
                            : row[1].toString();

                    return CategoryTotalResponse.builder()
                            .category((String) row[0])
                            .type(typeName)
                            .total((BigDecimal) row[2])
                            .build();
                })
                .collect(Collectors.toList());
    }
}
