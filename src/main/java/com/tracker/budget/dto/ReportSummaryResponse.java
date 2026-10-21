package com.tracker.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportSummaryResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private double savingsRate;
    private long totalTransactionCount;
    private BigDecimal averageDailyExpense;
    private String currencySymbol;

    private List<CategoryBreakdownItem> expenseCategories = new ArrayList<>();
    private List<CategoryBreakdownItem> incomeCategories = new ArrayList<>();
    private List<BudgetComparisonItem> budgetComparisons = new ArrayList<>();
    private List<DailySummaryItem> dailyBreakdown = new ArrayList<>();

    public ReportSummaryResponse() {
    }

    public static class CategoryBreakdownItem {
        private Long categoryId;
        private String categoryName;
        private String color;
        private String icon;
        private BigDecimal totalAmount;
        private double percentage;

        public CategoryBreakdownItem() {
        }

        public CategoryBreakdownItem(Long categoryId, String categoryName, String color, String icon, BigDecimal totalAmount, double percentage) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.color = color;
            this.icon = icon;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public static class BudgetComparisonItem {
        private String categoryName;
        private String color;
        private BigDecimal budgeted;
        private BigDecimal actual;
        private BigDecimal variance; // budgeted - actual
        private boolean overBudget;

        public BudgetComparisonItem() {
        }

        public BudgetComparisonItem(String categoryName, String color, BigDecimal budgeted, BigDecimal actual, BigDecimal variance, boolean overBudget) {
            this.categoryName = categoryName;
            this.color = color;
            this.budgeted = budgeted;
            this.actual = actual;
            this.variance = variance;
            this.overBudget = overBudget;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public BigDecimal getBudgeted() {
            return budgeted;
        }

        public void setBudgeted(BigDecimal budgeted) {
            this.budgeted = budgeted;
        }

        public BigDecimal getActual() {
            return actual;
        }

        public void setActual(BigDecimal actual) {
            this.actual = actual;
        }

        public BigDecimal getVariance() {
            return variance;
        }

        public void setVariance(BigDecimal variance) {
            this.variance = variance;
        }

        public boolean isOverBudget() {
            return overBudget;
        }

        public void setOverBudget(boolean overBudget) {
            this.overBudget = overBudget;
        }
    }

    public static class DailySummaryItem {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal expense;

        public DailySummaryItem() {
        }

        public DailySummaryItem(LocalDate date, BigDecimal income, BigDecimal expense) {
            this.date = date;
            this.income = income;
            this.expense = expense;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public void setIncome(BigDecimal income) {
            this.income = income;
        }

        public BigDecimal getExpense() {
            return expense;
        }

        public void setExpense(BigDecimal expense) {
            this.expense = expense;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public long getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public void setTotalTransactionCount(long totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    public BigDecimal getAverageDailyExpense() {
        return averageDailyExpense;
    }

    public void setAverageDailyExpense(BigDecimal averageDailyExpense) {
        this.averageDailyExpense = averageDailyExpense;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public List<CategoryBreakdownItem> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(List<CategoryBreakdownItem> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public List<CategoryBreakdownItem> getIncomeCategories() {
        return incomeCategories;
    }

    public void setIncomeCategories(List<CategoryBreakdownItem> incomeCategories) {
        this.incomeCategories = incomeCategories;
    }

    public List<BudgetComparisonItem> getBudgetComparisons() {
        return budgetComparisons;
    }

    public void setBudgetComparisons(List<BudgetComparisonItem> budgetComparisons) {
        this.budgetComparisons = budgetComparisons;
    }

    public List<DailySummaryItem> getDailyBreakdown() {
        return dailyBreakdown;
    }

    public void setDailyBreakdown(List<DailySummaryItem> dailyBreakdown) {
        this.dailyBreakdown = dailyBreakdown;
    }
}
