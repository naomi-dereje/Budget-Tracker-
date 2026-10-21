package com.tracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BudgetRequest {

    @NotBlank(message = "Year-Month is required (e.g., 2026-10)")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Year-Month must match YYYY-MM")
    private String yearMonth;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than zero")
    private BigDecimal monthlyLimit;

    private String notes;

    private List<CategoryAllocationItem> categoryAllocations = new ArrayList<>();

    public BudgetRequest() {
    }

    public BudgetRequest(String yearMonth, BigDecimal monthlyLimit, String notes) {
        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
        this.notes = notes;
    }

    public static class CategoryAllocationItem {
        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotNull(message = "Allocated amount is required")
        @DecimalMin(value = "0.01", message = "Allocated amount must be greater than zero")
        private BigDecimal allocatedAmount;

        public CategoryAllocationItem() {
        }

        public CategoryAllocationItem(Long categoryId, BigDecimal allocatedAmount) {
            this.categoryId = categoryId;
            this.allocatedAmount = allocatedAmount;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public BigDecimal getAllocatedAmount() {
            return allocatedAmount;
        }

        public void setAllocatedAmount(BigDecimal allocatedAmount) {
            this.allocatedAmount = allocatedAmount;
        }
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<CategoryAllocationItem> getCategoryAllocations() {
        return categoryAllocations;
    }

    public void setCategoryAllocations(List<CategoryAllocationItem> categoryAllocations) {
        this.categoryAllocations = categoryAllocations;
    }
}
