package com.finance.dashboard.service;

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
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository        = userRepository;
    }

    public PagedResponse<TransactionResponse> getTransactions(
            TransactionType type, String category,
            LocalDate startDate, LocalDate endDate,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        var spec = TransactionSpecification.withFilters(type, category, startDate, endDate);
        return PagedResponse.from(transactionRepository.findAll(spec, pageable), TransactionResponse::from);
    }

    public TransactionResponse getTransactionById(Long id) {
        return TransactionResponse.from(findActiveOrThrow(id));
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, String username) {
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

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Transaction transaction = findActiveOrThrow(id);
        if (request.getAmount() != null)   transaction.setAmount(request.getAmount());
        if (request.getType() != null)     transaction.setType(request.getType());
        if (request.getCategory() != null) transaction.setCategory(request.getCategory().trim());
        if (request.getDate() != null)     transaction.setDate(request.getDate());
        if (request.getNotes() != null)    transaction.setNotes(request.getNotes());
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = findActiveOrThrow(id);
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    private Transaction findActiveOrThrow(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }
}
