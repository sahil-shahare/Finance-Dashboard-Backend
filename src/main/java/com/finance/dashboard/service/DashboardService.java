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

    private static final int RECENT_TRANSACTION_LIMIT = 10;
    private static final int TREND_MONTHS = 12;

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // ── Cached read methods ──────────────────────────────────────────────────

    /**
     * Cached with key "global" — there is only one system-wide summary.
     * Evicted by evictDashboardCaches() whenever a transaction is written.
     *
     * Without caching, this fires 3 DB queries + a recent-transactions query
     * on every dashboard load. With Redis, the full object is served from
     * memory in < 1ms after the first request.
     */
    @Cacheable(value = CacheConstants.DASHBOARD_SUMMARY, key = "'global'")
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        List<TransactionResponse> recent = transactionRepository
                .findRecentTransactions(PageRequest.of(0, RECENT_TRANSACTION_LIMIT))
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

    /**
     * Cached with key "global" for the same reason as getSummary().
     * Computing 12 months of JPQL aggregation on every trend chart request
     * would be wasteful; Redis serves the result until a transaction write
     * invalidates it.
     */
    @Cacheable(value = CacheConstants.DASHBOARD_TRENDS, key = "'global'")
    public MonthlyTrendResponse getMonthlyTrends() {
        LocalDate since = LocalDate.now().minusMonths(TREND_MONTHS).withDayOfMonth(1);
        List<Object[]> rows = transactionRepository.monthlyTrends(since);

        Map<String, MonthlyEntry> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year   = ((Number) row[0]).intValue();
            int month  = ((Number) row[1]).intValue();
            String key = year + "-" + month;
            TransactionType type = TransactionType.valueOf((String) row[2]);
            BigDecimal amount    = (BigDecimal) row[3];

            map.computeIfAbsent(key, k -> MonthlyEntry.builder()
                    .year(year)
                    .month(month)
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

    // ── Cache eviction ───────────────────────────────────────────────────────

    /**
     * Called by TransactionService after every write (create / update / delete).
     * Clears all three dashboard caches so the next read recomputes fresh data.
     *
     * allEntries = true removes every key in the cache, not just "global",
     * which future-proofs against per-user dashboards being added later.
     */
    @CacheEvict(value = {
            CacheConstants.DASHBOARD_SUMMARY,
            CacheConstants.DASHBOARD_TRENDS,
            CacheConstants.CATEGORY_TOTALS
    }, allEntries = true)
    public void evictDashboardCaches() {
        // Spring AOP handles the eviction — no method body needed.
        // The method exists solely to be called from TransactionService.
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private List<CategoryTotalResponse> buildCategoryTotals() {
        return transactionRepository.sumByCategory()
                .stream()
                .map(row -> CategoryTotalResponse.builder()
                        .category((String) row[0])
                        .type(((TransactionType) row[1]).name())
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}
