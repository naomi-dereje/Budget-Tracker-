package com.tracker.budget.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryResponse {
    private String yearMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private BigDecimal monthlyBudgetLimit;
    private BigDecimal remainingBudget;
    private double budgetUtilizationRate;
    private boolean overBudget;
    private String currencySymbol;

    private List<CategorySpendingItem> categoryExpenses = new ArrayList<>();
    private List<MonthlyTrendItem> spendingTrends = new ArrayList<>();
    private List<TransactionResponse> recentTransactions = new ArrayList<>();

    public DashboardSummaryResponse() {
    }

    public static class CategorySpendingItem {
        private Long categoryId;
        private String categoryName;
        private String color;
        private String icon;
        private BigDecimal amount;
        private double percentage;

        public CategorySpendingItem() {
        }

        public CategorySpendingItem(Long categoryId, String categoryName, String color, String icon, BigDecimal amount, double percentage) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.color = color;
            this.icon = icon;
            this.amount = amount;
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

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public static class MonthlyTrendItem {
        private String month; // e.g. "Jun", "Jul", "2026-06"
        private BigDecimal income;
        private BigDecimal expense;

        public MonthlyTrendItem() {
        }

        public MonthlyTrendItem(String month, BigDecimal income, BigDecimal expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
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

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
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

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public BigDecimal getMonthlyBudgetLimit() {
        return monthlyBudgetLimit;
    }

    public void setMonthlyBudgetLimit(BigDecimal monthlyBudgetLimit) {
        this.monthlyBudgetLimit = monthlyBudgetLimit;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public double getBudgetUtilizationRate() {
        return budgetUtilizationRate;
    }

    public void setBudgetUtilizationRate(double budgetUtilizationRate) {
        this.budgetUtilizationRate = budgetUtilizationRate;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public List<CategorySpendingItem> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<CategorySpendingItem> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }

    public List<MonthlyTrendItem> getSpendingTrends() {
        return spendingTrends;
    }

    public void setSpendingTrends(List<MonthlyTrendItem> spendingTrends) {
        this.spendingTrends = spendingTrends;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
