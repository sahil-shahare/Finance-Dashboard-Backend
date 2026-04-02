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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TransactionService transactionService;

    private User adminUser;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .password("hashed")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        sampleTransaction = Transaction.builder()
                .id(10L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2025, 1, 15))
                .notes("January salary")
                .createdBy(adminUser)
                .deleted(false)
                .build();
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

        TransactionResponse response = transactionService.getTransactionById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCategory()).isEqualTo("Salary");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
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
    @DisplayName("createTransaction: persists and returns new transaction")
    void createTransaction_success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("250.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory("Utilities");
        request.setDate(LocalDate.of(2025, 2, 1));
        request.setNotes("Electric bill");

        Transaction saved = Transaction.builder()
                .id(11L)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(adminUser)
                .deleted(false)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(request, "admin");

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getCategory()).isEqualTo("Utilities");
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // ── updateTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTransaction: applies only supplied fields")
    void updateTransaction_partialUpdate() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setCategory("Freelance");   // only update category

        when(transactionRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.updateTransaction(10L, request);

        assertThat(response.getCategory()).isEqualTo("Freelance");
        // Amount and type should remain unchanged
        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
    }

    // ── deleteTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTransaction: sets deleted=true without removing the row")
    void deleteTransaction_softDelete() {
        when(transactionRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        transactionService.deleteTransaction(10L);

        assertThat(sampleTransaction.isDeleted()).isTrue();
        verify(transactionRepository, times(1)).save(sampleTransaction);
    }

    @Test
    @DisplayName("deleteTransaction: throws ResourceNotFoundException for unknown id")
    void deleteTransaction_notFound() {
        when(transactionRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).save(any());
    }
}
