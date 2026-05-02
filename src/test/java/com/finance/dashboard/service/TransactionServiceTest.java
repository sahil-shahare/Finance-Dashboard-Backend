package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.CreateTransactionRequest;
import com.finance.dashboard.dto.request.UpdateTransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.model.enums.UserStatus;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository        userRepository;
    /**
     * FIX: TransactionService now depends on DashboardService to call
     * evictDashboardCaches() after every write. Without this mock,
     * @InjectMocks leaves dashboardService null → NullPointerException
     * on createTransaction / updateTransaction / deleteTransaction.
     */
    @Mock private DashboardService dashboardService;

    @InjectMocks private TransactionService transactionService;

    private User        adminUser;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).username("admin").email("admin@example.com")
                .password("hashed").role(Role.ADMIN).status(UserStatus.ACTIVE)
                .build();

        sampleTransaction = Transaction.builder()
                .id(10L).amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME).category("Salary")
                .date(LocalDate.of(2025, 1, 15)).notes("January salary")
                .createdBy(adminUser).deleted(false).build();
    }

    // ── getTransactions ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getTransactions: returns paged list of active transactions")
    void getTransactions_returnsPagedList() {
        var page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<TransactionResponse> result =
                transactionService.getTransactions(null, null, null, null, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ── getTransactionById ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTransactionById: returns transaction when found")
    void getTransactionById_found() {
        when(transactionRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(sampleTransaction));

        TransactionResponse resp = transactionService.getTransactionById(10L);

        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getCategory()).isEqualTo("Salary");
        assertThat(resp.getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("getTransactionById: throws ResourceNotFoundException when not found")
    void getTransactionById_notFound() {
        when(transactionRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createTransaction: saves record and evicts dashboard cache")
    void createTransaction_success() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setAmount(new BigDecimal("250.00"));
        req.setType(TransactionType.EXPENSE);
        req.setCategory("Utilities");
        req.setDate(LocalDate.of(2025, 2, 1));
        req.setNotes("Electric bill");

        Transaction saved = Transaction.builder()
                .id(11L).amount(req.getAmount()).type(req.getType())
                .category(req.getCategory()).date(req.getDate())
                .notes(req.getNotes()).createdBy(adminUser).deleted(false).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse resp = transactionService.createTransaction(req, "admin");

        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getCategory()).isEqualTo("Utilities");
        assertThat(resp.getType()).isEqualTo(TransactionType.EXPENSE);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        // Dashboard cache must be evicted after every write
        verify(dashboardService, times(1)).evictDashboardCaches();
    }

    // ── updateTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTransaction: applies only supplied fields and evicts cache")
    void updateTransaction_partialUpdate() {
        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setCategory("Freelance");   // only update category

        when(transactionRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse resp = transactionService.updateTransaction(10L, req);

        assertThat(resp.getCategory()).isEqualTo("Freelance");
        // Amount and type must remain unchanged
        assertThat(resp.getAmount()).isEqualByComparingTo("500.00");
        assertThat(resp.getType()).isEqualTo(TransactionType.INCOME);

        verify(dashboardService, times(1)).evictDashboardCaches();
    }

    // ── deleteTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTransaction: sets deleted=true without removing row, evicts cache")
    void deleteTransaction_softDelete() {
        when(transactionRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        transactionService.deleteTransaction(10L);

        assertThat(sampleTransaction.isDeleted()).isTrue();
        verify(transactionRepository, times(1)).save(sampleTransaction);
        verify(dashboardService, times(1)).evictDashboardCaches();
    }

    @Test
    @DisplayName("deleteTransaction: throws ResourceNotFoundException for unknown id")
    void deleteTransaction_notFound() {
        when(transactionRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).save(any());
        verify(dashboardService, never()).evictDashboardCaches();
    }
}
