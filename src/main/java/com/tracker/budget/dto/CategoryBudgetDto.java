package com.tracker.budget.dto;

import java.math.BigDecimal;

public class CategoryBudgetDto {
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private BigDecimal allocatedAmount;
    private BigDecimal actualSpent;
    private BigDecimal remainingAmount;
    private double percentageUsed;
    private boolean overBudget;

    public CategoryBudgetDto() {
    }

    public CategoryBudgetDto(Long categoryId, String categoryName, String categoryColor, String categoryIcon,
                             BigDecimal allocatedAmount, BigDecimal actualSpent, BigDecimal remainingAmount,
                             double percentageUsed, boolean overBudget) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.categoryIcon = categoryIcon;
        this.allocatedAmount = allocatedAmount;
        this.actualSpent = actualSpent;
        this.remainingAmount = remainingAmount;
        this.percentageUsed = percentageUsed;
        this.overBudget = overBudget;
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

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public BigDecimal getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(BigDecimal actualSpent) {
        this.actualSpent = actualSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
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
}
