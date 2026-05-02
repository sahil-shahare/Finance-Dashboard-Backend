package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.CreateTransactionRequest;
import com.finance.dashboard.dto.request.UpdateTransactionRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    public TransactionController(TransactionService s){this.transactionService=s;}
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getAll(@RequestParam(required=false) TransactionType type,@RequestParam(required=false) String category,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ResponseEntity.ok(ApiResponse.success(transactionService.getTransactions(type,category,startDate,endDate,page,size)));}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success(transactionService.getTransactionById(id)));}
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@Valid @RequestBody CreateTransactionRequest req,@AuthenticationPrincipal UserDetails ud){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transaction created",transactionService.createTransaction(req,ud.getUsername())));}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(@PathVariable Long id,@Valid @RequestBody UpdateTransactionRequest req){return ResponseEntity.ok(ApiResponse.success("Transaction updated",transactionService.updateTransaction(id,req)));}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){transactionService.deleteTransaction(id);return ResponseEntity.ok(ApiResponse.success("Transaction deleted (soft)",null));}
}
