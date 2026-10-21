package com.tracker.budget.service;

import com.tracker.budget.dto.TransactionRequest;
import com.tracker.budget.dto.TransactionResponse;
import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.Transaction;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.exception.UnauthorizedException;
import com.tracker.budget.repository.TransactionRepository;
import com.tracker.budget.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alex@campus.edu");

        expenseCategory = new Category(null, "Groceries", TransactionType.EXPENSE, "utensils", "#EF4444", true);
        expenseCategory.setId(101L);

        incomeCategory = new Category(null, "Part-Time Job", TransactionType.INCOME, "briefcase", "#10B981", true);
        incomeCategory.setId(201L);
    }

    @Test
    @DisplayName("Should create an expense transaction with decimal amount successfully")
    void createTransaction_Expense_Success() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                101L,
                new BigDecimal("49.95"),
                "Weekly food market haul",
                LocalDate.of(2026, 10, 15)
        );

        when(authService.getCurrentUserEntity()).thenReturn(testUser);
        when(categoryService.getCategoryEntityForUser(101L)).thenReturn(expenseCategory);

        Transaction savedTx = new Transaction(testUser, expenseCategory, TransactionType.EXPENSE,
                new BigDecimal("49.95"), "Weekly food market haul", LocalDate.of(2026, 10, 15));
        savedTx.setId(500L);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(new BigDecimal("49.95"), response.getAmount());
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals("Weekly food market haul", response.getDescription());
        assertEquals(101L, response.getCategoryId());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject zero or negative transaction amounts")
    void createTransaction_NegativeAmount_ThrowsException() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                101L,
                new BigDecimal("-15.00"),
                "Invalid negative refund",
                LocalDate.now()
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertTrue(ex.getMessage().contains("greater than zero"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject category type mismatch (e.g. Income category on Expense transaction)")
    void createTransaction_CategoryTypeMismatch_ThrowsException() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                201L, // Income category
                new BigDecimal("100.00"),
                "Mismatched transaction",
                LocalDate.now()
        );

        when(authService.getCurrentUserEntity()).thenReturn(testUser);
        when(categoryService.getCategoryEntityForUser(201L)).thenReturn(incomeCategory);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertTrue(ex.getMessage().contains("does not match"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should enforce data isolation: prevent User B from deleting User A's transaction")
    void deleteTransaction_UnauthorizedUser_ThrowsException() {
        User userA = new User();
        userA.setId(1L);

        Transaction tx = new Transaction(userA, expenseCategory, TransactionType.EXPENSE,
                new BigDecimal("30.00"), "User A transaction", LocalDate.now());
        tx.setId(777L);

        when(securityUtils.getCurrentUserId()).thenReturn(2L); // User B logged in
        when(transactionRepository.findById(777L)).thenReturn(Optional.of(tx));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
            transactionService.deleteTransaction(777L);
        });

        assertTrue(ex.getMessage().contains("do not have permission"));
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    @DisplayName("Should successfully delete transaction when owned by current user")
    void deleteTransaction_Owner_Success() {
        Transaction tx = new Transaction(testUser, expenseCategory, TransactionType.EXPENSE,
                new BigDecimal("30.00"), "Test transaction", LocalDate.now());
        tx.setId(777L);

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findById(777L)).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction(777L);

        verify(transactionRepository, times(1)).delete(tx);
    }
}
