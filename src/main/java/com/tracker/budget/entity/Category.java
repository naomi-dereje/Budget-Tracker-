package com.tracker.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_user", columnList = "user_id"),
        @Index(name = "idx_category_type", columnList = "type")
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Category name is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    @NotNull(message = "Category type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String icon = "tag";

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String color = "#6366F1";

    @Column(name = "is_system_default", nullable = false)
    private boolean isSystemDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Category() {
    }

    public Category(User user, String name, TransactionType type, String icon, String color, boolean isSystemDefault) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isSystemDefault = isSystemDefault;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSystemDefault() {
        return isSystemDefault;
    }

    public void setSystemDefault(boolean systemDefault) {
        isSystemDefault = systemDefault;
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
