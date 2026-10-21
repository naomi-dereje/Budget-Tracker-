package com.tracker.budget.dto;

import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.TransactionType;

public class CategoryResponse {
    private Long id;
    private String name;
    private TransactionType type;
    private String icon;
    private String color;
    private boolean isSystemDefault;
    private boolean isCustom;

    public CategoryResponse() {
    }

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType();
        this.icon = category.getIcon();
        this.color = category.getColor();
        this.isSystemDefault = category.isSystemDefault();
        this.isCustom = !category.isSystemDefault();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
