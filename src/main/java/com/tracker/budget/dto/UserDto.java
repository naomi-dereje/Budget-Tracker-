package com.tracker.budget.dto;

import java.math.BigDecimal;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String university;
    private String currencySymbol;
    private BigDecimal monthlyBudgetBaseline;

    public UserDto() {
    }

    public UserDto(Long id, String email, String fullName, String university, String currencySymbol, BigDecimal monthlyBudgetBaseline) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.university = university;
        this.currencySymbol = currencySymbol;
        this.monthlyBudgetBaseline = monthlyBudgetBaseline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public BigDecimal getMonthlyBudgetBaseline() {
        return monthlyBudgetBaseline;
    }

    public void setMonthlyBudgetBaseline(BigDecimal monthlyBudgetBaseline) {
        this.monthlyBudgetBaseline = monthlyBudgetBaseline;
    }
}
