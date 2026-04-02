package com.finance.dashboard.repository;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    // Single record lookup — excludes soft-deleted entries
    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    // Used by dashboard: total by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Category-wise totals (for pie / bar charts)
    @Query("""
            SELECT t.category, t.type, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deleted = false
            GROUP BY t.category, t.type
            ORDER BY t.category
            """)
    List<Object[]> sumByCategory();

    // Monthly aggregation for trend charts — last N months
    @Query("""
            SELECT FUNCTION('YEAR', t.date)  AS yr,
                   FUNCTION('MONTH', t.date) AS mo,
                   t.type,
                   COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deleted = false
              AND t.date >= :since
            GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type
            ORDER BY yr, mo
            """)
    List<Object[]> monthlyTrends(@Param("since") LocalDate since);

    // Recent transactions for activity feed
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.date DESC, t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);
}
