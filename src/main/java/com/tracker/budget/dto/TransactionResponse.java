package com.tracker.budget.dto;

import com.tracker.budget.entity.Transaction;
import com.tracker.budget.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.type = transaction.getType();
        if (transaction.getCategory() != null) {
            this.categoryId = transaction.getCategory().getId();
            this.categoryName = transaction.getCategory().getName();
            this.categoryColor = transaction.getCategory().getColor();
            this.categoryIcon = transaction.getCategory().getIcon();
        }
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.transactionDate = transaction.getTransactionDate();
        this.createdAt = transaction.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
