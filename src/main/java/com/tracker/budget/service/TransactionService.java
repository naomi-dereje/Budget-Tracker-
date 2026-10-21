package com.tracker.budget.service;

import com.tracker.budget.dto.TransactionRequest;
import com.tracker.budget.dto.TransactionResponse;
import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.Transaction;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.exception.ResourceNotFoundException;
import com.tracker.budget.exception.UnauthorizedException;
import com.tracker.budget.repository.TransactionRepository;
import com.tracker.budget.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              AuthService authService,
                              SecurityUtils securityUtils) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateTransactionRequest(request);

        User currentUser = authService.getCurrentUserEntity();
        Category category = categoryService.getCategoryEntityForUser(request.getCategoryId());

        if (category.getType() != request.getType()) {
            throw new BadRequestException("Selected category type (" + category.getType() + ") does not match transaction type (" + request.getType() + ").");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription().trim());
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction saved = transactionRepository.save(transaction);
        return new TransactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getFilteredTransactions(
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable) {

        Long userId = securityUtils.getCurrentUserId();
        Page<Transaction> page = transactionRepository.findFilteredTransactions(
                userId, type, categoryId, startDate, endDate, search, pageable
        );

        return page.map(TransactionResponse::new);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to view this transaction.");
        }

        return new TransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        validateTransactionRequest(request);

        Long userId = securityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this transaction.");
        }

        Category category = categoryService.getCategoryEntityForUser(request.getCategoryId());
        if (category.getType() != request.getType()) {
            throw new BadRequestException("Selected category type (" + category.getType() + ") does not match transaction type (" + request.getType() + ").");
        }

        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription().trim());
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction updated = transactionRepository.save(transaction);
        return new TransactionResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this transaction.");
        }

        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions() {
        Long userId = securityUtils.getCurrentUserId();
        return transactionRepository.findTop10ByUserIdOrderByTransactionDateDescIdDesc(userId).stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be greater than zero.");
        }
        if (request.getTransactionDate() == null) {
            throw new BadRequestException("Transaction date cannot be null.");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description cannot be blank.");
        }
    }
}
