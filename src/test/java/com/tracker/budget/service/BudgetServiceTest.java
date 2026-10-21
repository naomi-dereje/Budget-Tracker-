package com.tracker.budget.service;

import com.tracker.budget.dto.BudgetRequest;
import com.tracker.budget.dto.BudgetResponse;
import com.tracker.budget.entity.*;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.repository.BudgetRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category groceriesCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alex@campus.edu");
        testUser.setMonthlyBudgetBaseline(new BigDecimal("1000.00"));

        groceriesCategory = new Category(null, "Groceries", TransactionType.EXPENSE, "utensils", "#EF4444", true);
        groceriesCategory.setId(10L);
    }

    @Test
    @DisplayName("Should calculate monthly budget utilization and warning flags correctly")
    void getBudgetForMonth_ExistingBudget_CalculatesCorrectly() {
        Budget budget = new Budget(testUser, "2026-10", new BigDecimal("1000.00"), "October Semester Budget");
        budget.setId(5L);

        CategoryBudget cb = new CategoryBudget(budget, groceriesCategory, new BigDecimal("300.00"));
        budget.addCategoryBudget(cb);

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(budgetRepository.findByUserIdAndYearMonthWithCategories(1L, "2026-10")).thenReturn(Optional.of(budget));

        // Total actual expense for month: 850.00 (85% used -> warning)
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(new BigDecimal("850.00"));

        // Spent in groceries category: 320.00 (> 300.00 -> over budget)
        when(transactionRepository.sumExpenseByCategoryAndDateBetween(
                eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(new BigDecimal("320.00"));

        BudgetResponse response = budgetService.getBudgetForMonth("2026-10");

        assertNotNull(response);
        assertEquals("2026-10", response.getYearMonth());
        assertEquals(new BigDecimal("1000.00"), response.getMonthlyLimit());
        assertEquals(new BigDecimal("850.00"), response.getTotalSpent());
        assertEquals(new BigDecimal("150.00"), response.getRemainingBudget());
        assertEquals(85.0, response.getPercentageUsed());
        assertFalse(response.isOverBudget()); // Overall is 85% <= 100%

        // Category budget check
        assertEquals(1, response.getCategoryBudgets().size());
        assertEquals(new BigDecimal("300.00"), response.getCategoryBudgets().get(0).getAllocatedAmount());
        assertEquals(new BigDecimal("320.00"), response.getCategoryBudgets().get(0).getActualSpent());
        assertEquals(new BigDecimal("-20.00"), response.getCategoryBudgets().get(0).getRemainingAmount());
        assertTrue(response.getCategoryBudgets().get(0).isOverBudget());
    }

    @Test
    @DisplayName("Should handle edge case: User with no transactions in current month")
    void getBudgetForMonth_NoTransactions_ReturnsZeroSpent() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(budgetRepository.findByUserIdAndYearMonthWithCategories(1L, "2026-11")).thenReturn(Optional.empty());
        when(authService.getCurrentUserEntity()).thenReturn(testUser);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(null); // No transactions in DB returns null from SUM

        BudgetResponse response = budgetService.getBudgetForMonth("2026-11");

        assertNotNull(response);
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalSpent()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getRemainingBudget()));
        assertEquals(0.0, response.getPercentageUsed());
        assertFalse(response.isOverBudget());
    }

    @Test
    @DisplayName("Should reject budget creation with invalid format or zero limit")
    void createBudget_InvalidInput_ThrowsBadRequestException() {
        BudgetRequest request = new BudgetRequest("invalid-date", new BigDecimal("-100.00"), "Notes");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            budgetService.createOrUpdateBudget(request);
        });

        assertTrue(ex.getMessage().contains("format") || ex.getMessage().contains("greater than zero"));
    }
}
