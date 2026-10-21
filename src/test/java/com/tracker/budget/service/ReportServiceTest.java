package com.tracker.budget.service;

import com.tracker.budget.dto.ReportSummaryResponse;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.entity.User;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ReportService reportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alex@campus.edu");
        testUser.setCurrencySymbol("$");
    }

    @Test
    @DisplayName("Should accurately calculate income, expenses, net savings, and savings rate")
    void generateReport_CalculatesFinancialMetricsAccurately() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(authService.getCurrentUserEntity()).thenReturn(testUser);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)
        )).thenReturn(new BigDecimal("1500.00"));

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)
        )).thenReturn(new BigDecimal("900.00"));

        when(transactionRepository.getCategoryBreakdown(eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.getCategoryBreakdown(eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.findAllInDateRange(eq(1L), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());

        com.tracker.budget.dto.BudgetResponse mockBudget = new com.tracker.budget.dto.BudgetResponse();
        mockBudget.setMonthlyLimit(new BigDecimal("1000.00"));
        mockBudget.setCategoryBudgets(Collections.emptyList());
        when(budgetService.getBudgetForMonth(any())).thenReturn(mockBudget);

        // Call report service
        ReportSummaryResponse report = reportService.generateReport(start, end);

        assertNotNull(report);
        assertEquals(new BigDecimal("1500.00"), report.getTotalIncome());
        assertEquals(new BigDecimal("900.00"), report.getTotalExpenses());
        assertEquals(new BigDecimal("600.00"), report.getNetSavings());
        // Savings rate = (600 / 1500) * 100 = 40.0%
        assertEquals(40.0, report.getSavingsRate());
        assertEquals("$", report.getCurrencySymbol());
    }
}
