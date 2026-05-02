package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.request.CreateTransactionRequest;
import com.finance.dashboard.dto.request.UpdateTransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.TransactionSpecification;
import com.finance.dashboard.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;
    private final DashboardService      dashboardService;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              DashboardService dashboardService) {
        this.transactionRepository = transactionRepository;
        this.userRepository        = userRepository;
        this.dashboardService      = dashboardService;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Paginated + filtered list — NOT cached intentionally.
     * The combination of 4 optional filters × page × size produces too many
     * distinct cache keys for Redis to manage efficiently. List queries hit
     * MySQL directly; individual record lookups benefit from caching instead.
     */
    public PagedResponse<TransactionResponse> getTransactions(
            TransactionType type, String category,
            LocalDate startDate, LocalDate endDate,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        var spec = TransactionSpecification.withFilters(type, category, startDate, endDate);
        return PagedResponse.from(transactionRepository.findAll(spec, pageable),
                TransactionResponse::from);
    }

    /**
     * Cached by transaction ID.
     * Detail views are frequent (user clicks a row to see full details) and
     * the record rarely changes once created — ideal for caching.
     */
    @Cacheable(value = CacheConstants.TRANSACTION_BY_ID, key = "#id")
    public TransactionResponse getTransactionById(Long id) {
        return TransactionResponse.from(findActiveOrThrow(id));
    }

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Creates a transaction.
     *
     * Cache strategy:
     *   - No entry is added to TRANSACTION_BY_ID here (will be populated on
     *     first GET).
     *   - Dashboard caches are evicted because all aggregations are now stale.
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request,
                                                 String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .build();

        TransactionResponse saved = TransactionResponse.from(
                transactionRepository.save(transaction));

        // Evict dashboard aggregations — totals have changed
        dashboardService.evictDashboardCaches();

        return saved;
    }

    /**
     * Updates a transaction.
     *
     * @CachePut refreshes the cached record with the new data after the update.
     * Dashboard caches are also evicted since amounts/types may have changed.
     */
    @Transactional
    @CachePut(value = CacheConstants.TRANSACTION_BY_ID, key = "#id")
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Transaction transaction = findActiveOrThrow(id);

        if (request.getAmount() != null)   transaction.setAmount(request.getAmount());
        if (request.getType() != null)     transaction.setType(request.getType());
        if (request.getCategory() != null) transaction.setCategory(request.getCategory().trim());
        if (request.getDate() != null)     transaction.setDate(request.getDate());
        if (request.getNotes() != null)    transaction.setNotes(request.getNotes());

        TransactionResponse updated = TransactionResponse.from(
                transactionRepository.save(transaction));

        // Evict dashboard aggregations — amounts/categories may have changed
        dashboardService.evictDashboardCaches();

        return updated;
    }

    /**
     * Soft-deletes a transaction.
     *
     * @CacheEvict removes the cached record since it must no longer be served.
     * Dashboard caches are also evicted since totals have changed.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.TRANSACTION_BY_ID, key = "#id")
    public void deleteTransaction(Long id) {
        Transaction transaction = findActiveOrThrow(id);
        transaction.setDeleted(true);
        transactionRepository.save(transaction);

        // Evict dashboard aggregations
        dashboardService.evictDashboardCaches();
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private Transaction findActiveOrThrow(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id));
    }
}
