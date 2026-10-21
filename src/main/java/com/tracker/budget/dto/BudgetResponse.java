package com.tracker.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BudgetResponse {
    private Long id;
    private String yearMonth;
    private BigDecimal monthlyLimit;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private double percentageUsed;
    private boolean overBudget;
    private String notes;
    private List<CategoryBudgetDto> categoryBudgets = new ArrayList<>();
    private LocalDateTime updatedAt;

    public BudgetResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<CategoryBudgetDto> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(List<CategoryBudgetDto> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
