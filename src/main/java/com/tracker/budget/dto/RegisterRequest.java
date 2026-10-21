package com.tracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    @Size(max = 150, message = "University name cannot exceed 150 characters")
    private String university;

    @Size(max = 5, message = "Currency symbol cannot exceed 5 characters")
    private String currencySymbol = "$";

    @DecimalMin(value = "0.00", message = "Baseline budget cannot be negative")
    private BigDecimal monthlyBudgetBaseline = BigDecimal.ZERO;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String password, String university) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.university = university;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
