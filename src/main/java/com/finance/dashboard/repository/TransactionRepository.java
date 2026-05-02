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
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndDeletedFalse(Long id);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);
    @Query("SELECT t.category, t.type, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deleted = false GROUP BY t.category, t.type ORDER BY t.category")
    List<Object[]> sumByCategory();
    @Query("SELECT FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deleted = false AND t.date >= :since GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type ORDER BY 1, 2")
    List<Object[]> monthlyTrends(@Param("since") LocalDate since);
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.date DESC, t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);
}
