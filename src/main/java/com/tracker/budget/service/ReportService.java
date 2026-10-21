package com.tracker.budget.service;

import com.tracker.budget.dto.BudgetResponse;
import com.tracker.budget.dto.DashboardSummaryResponse;
import com.tracker.budget.dto.ReportSummaryResponse;
import com.tracker.budget.dto.TransactionResponse;
import com.tracker.budget.entity.Transaction;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.entity.User;
import com.tracker.budget.repository.TransactionRepository;
import com.tracker.budget.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public ReportService(TransactionRepository transactionRepository,
                         BudgetService budgetService,
                         AuthService authService,
                         SecurityUtils securityUtils) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String yearMonthStr) {
        Long userId = securityUtils.getCurrentUserId();
        User user = authService.getCurrentUserEntity();

        String yearMonth = (yearMonthStr != null && !yearMonthStr.isBlank())
                ? yearMonthStr.trim()
                : YearMonth.now().toString();

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startDate, endDate
        );
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Budget info
        BudgetResponse budgetResponse = budgetService.getBudgetForMonth(yearMonth);
        BigDecimal budgetLimit = budgetResponse.getMonthlyLimit();
        BigDecimal remainingBudget = budgetLimit.subtract(totalExpenses);

        double budgetUtilization = 0.0;
        boolean overBudget = false;
        if (budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            double rate = totalExpenses.divide(budgetLimit, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            budgetUtilization = Math.round(rate * 10.0) / 10.0;
            overBudget = totalExpenses.compareTo(budgetLimit) > 0;
        }

        // Category breakdown for expenses
        List<TransactionRepository.CategorySummaryProjection> categoryProjections =
                transactionRepository.getCategoryBreakdown(userId, TransactionType.EXPENSE, startDate, endDate);

        List<DashboardSummaryResponse.CategorySpendingItem> categoryItems = new ArrayList<>();
        for (TransactionRepository.CategorySummaryProjection p : categoryProjections) {
            double pct = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = p.getTotalAmount().divide(totalExpenses, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                pct = Math.round(pct * 10.0) / 10.0;
            }
            categoryItems.add(new DashboardSummaryResponse.CategorySpendingItem(
                    p.getCategoryId(),
                    p.getCategoryName(),
                    p.getColor(),
                    p.getIcon(),
                    p.getTotalAmount(),
                    pct
            ));
        }

        // Monthly trends for past 6 months up to selected yearMonth
        List<DashboardSummaryResponse.MonthlyTrendItem> trendItems = new ArrayList<>();
        YearMonth currentIter = ym.minusMonths(5);
        DateTimeFormatter trendFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        while (!currentIter.isAfter(ym)) {
            LocalDate mStart = currentIter.atDay(1);
            LocalDate mEnd = currentIter.atEndOfMonth();

            BigDecimal mIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.INCOME, mStart, mEnd
            );
            if (mIncome == null) mIncome = BigDecimal.ZERO;

            BigDecimal mExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.EXPENSE, mStart, mEnd
            );
            if (mExpense == null) mExpense = BigDecimal.ZERO;

            trendItems.add(new DashboardSummaryResponse.MonthlyTrendItem(
                    currentIter.format(trendFormatter),
                    mIncome,
                    mExpense
            ));

            currentIter = currentIter.plusMonths(1);
        }

        // Recent 8 transactions
        List<TransactionResponse> recentTransactions = transactionRepository.findTop10ByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream()
                .limit(8)
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setYearMonth(yearMonth);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetBalance(netBalance);
        response.setMonthlyBudgetLimit(budgetLimit);
        response.setRemainingBudget(remainingBudget);
        response.setBudgetUtilizationRate(budgetUtilization);
        response.setOverBudget(overBudget);
        response.setCurrencySymbol(user.getCurrencySymbol());
        response.setCategoryExpenses(categoryItems);
        response.setSpendingTrends(trendItems);
        response.setRecentTransactions(recentTransactions);

        return response;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse generateReport(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUtils.getCurrentUserId();
        User user = authService.getCurrentUserEntity();

        if (startDate == null) {
            startDate = YearMonth.now().atDay(1);
        }
        if (endDate == null) {
            endDate = YearMonth.now().atEndOfMonth();
        }
        if (endDate.isBefore(startDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startDate, endDate
        );
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            double rate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            savingsRate = Math.round(rate * 10.0) / 10.0;
        }

        long dayCount = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);
        BigDecimal avgDailyExpense = totalExpenses.divide(BigDecimal.valueOf(dayCount), 2, RoundingMode.HALF_UP);

        // Expense category breakdown
        List<TransactionRepository.CategorySummaryProjection> expProjections =
                transactionRepository.getCategoryBreakdown(userId, TransactionType.EXPENSE, startDate, endDate);
        List<ReportSummaryResponse.CategoryBreakdownItem> expenseCategories = new ArrayList<>();
        for (TransactionRepository.CategorySummaryProjection p : expProjections) {
            double pct = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? Math.round(p.getTotalAmount().divide(totalExpenses, 4, RoundingMode.HALF_UP).doubleValue() * 1000.0) / 10.0
                    : 0.0;
            expenseCategories.add(new ReportSummaryResponse.CategoryBreakdownItem(
                    p.getCategoryId(), p.getCategoryName(), p.getColor(), p.getIcon(), p.getTotalAmount(), pct
            ));
        }

        // Income category breakdown
        List<TransactionRepository.CategorySummaryProjection> incProjections =
                transactionRepository.getCategoryBreakdown(userId, TransactionType.INCOME, startDate, endDate);
        List<ReportSummaryResponse.CategoryBreakdownItem> incomeCategories = new ArrayList<>();
        for (TransactionRepository.CategorySummaryProjection p : incProjections) {
            double pct = totalIncome.compareTo(BigDecimal.ZERO) > 0
                    ? Math.round(p.getTotalAmount().divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() * 1000.0) / 10.0
                    : 0.0;
            incomeCategories.add(new ReportSummaryResponse.CategoryBreakdownItem(
                    p.getCategoryId(), p.getCategoryName(), p.getColor(), p.getIcon(), p.getTotalAmount(), pct
            ));
        }

        // Daily breakdown and transaction count
        List<Transaction> transactions = transactionRepository.findAllInDateRange(userId, startDate, endDate);
        Map<LocalDate, BigDecimal[]> dailyMap = new TreeMap<>();
        for (Transaction t : transactions) {
            dailyMap.putIfAbsent(t.getTransactionDate(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] pair = dailyMap.get(t.getTransactionDate());
            if (t.getType() == TransactionType.INCOME) {
                pair[0] = pair[0].add(t.getAmount());
            } else {
                pair[1] = pair[1].add(t.getAmount());
            }
        }

        List<ReportSummaryResponse.DailySummaryItem> dailyList = new ArrayList<>();
        dailyMap.forEach((date, arr) -> dailyList.add(new ReportSummaryResponse.DailySummaryItem(date, arr[0], arr[1])));

        // Budget vs Actual for the latest month in range
        YearMonth ym = YearMonth.from(endDate);
        BudgetResponse budgetInfo = budgetService.getBudgetForMonth(ym.toString());
        List<ReportSummaryResponse.BudgetComparisonItem> budgetComparisons = new ArrayList<>();

        if (budgetInfo != null && budgetInfo.getCategoryBudgets() != null && !budgetInfo.getCategoryBudgets().isEmpty()) {
            budgetInfo.getCategoryBudgets().forEach(cb -> {
                BigDecimal variance = cb.getAllocatedAmount().subtract(cb.getActualSpent());
                budgetComparisons.add(new ReportSummaryResponse.BudgetComparisonItem(
                        cb.getCategoryName(),
                        cb.getCategoryColor(),
                        cb.getAllocatedAmount(),
                        cb.getActualSpent(),
                        variance,
                        cb.isOverBudget()
                ));
            });
        }

        ReportSummaryResponse response = new ReportSummaryResponse();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(netSavings);
        response.setSavingsRate(savingsRate);
        response.setTotalTransactionCount(transactions.size());
        response.setAverageDailyExpense(avgDailyExpense);
        response.setCurrencySymbol(user.getCurrencySymbol());
        response.setExpenseCategories(expenseCategories);
        response.setIncomeCategories(incomeCategories);
        response.setDailyBreakdown(dailyList);
        response.setBudgetComparisons(budgetComparisons);

        return response;
    }
}
