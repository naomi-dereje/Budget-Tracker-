package com.tracker.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_user_month", columnNames = {"user_id", "year_month"})
}, indexes = {
        @Index(name = "idx_budget_user_month", columnList = "user_id, year_month")
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Year-Month is required (format: YYYY-MM)")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Year-Month must match YYYY-MM")
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than 0")
    @Column(name = "monthly_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(length = 255)
    private String notes;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Budget() {
    }

    public Budget(User user, String yearMonth, BigDecimal monthlyLimit, String notes) {
        this.user = user;
        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
        this.notes = notes;
    }

    public void addCategoryBudget(CategoryBudget categoryBudget) {
        categoryBudgets.add(categoryBudget);
        categoryBudget.setBudget(this);
    }

    public void removeCategoryBudget(CategoryBudget categoryBudget) {
        categoryBudgets.remove(categoryBudget);
        categoryBudget.setBudget(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<CategoryBudget> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(List<CategoryBudget> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
