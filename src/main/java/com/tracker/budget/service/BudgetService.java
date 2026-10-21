package com.tracker.budget.service;

import com.tracker.budget.dto.BudgetRequest;
import com.tracker.budget.dto.BudgetResponse;
import com.tracker.budget.dto.CategoryBudgetDto;
import com.tracker.budget.entity.*;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.exception.ResourceNotFoundException;
import com.tracker.budget.exception.UnauthorizedException;
import com.tracker.budget.repository.BudgetRepository;
import com.tracker.budget.repository.TransactionRepository;
import com.tracker.budget.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public BudgetService(BudgetRepository budgetRepository,
                         TransactionRepository transactionRepository,
                         CategoryService categoryService,
                         AuthService authService,
                         SecurityUtils securityUtils) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetForMonth(String yearMonthStr) {
        Long userId = securityUtils.getCurrentUserId();
        String yearMonth = (yearMonthStr != null && !yearMonthStr.isBlank())
                ? yearMonthStr.trim()
                : YearMonth.now().toString();

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndYearMonthWithCategories(userId, yearMonth);

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal actualTotalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (actualTotalExpense == null) actualTotalExpense = BigDecimal.ZERO;

        if (budgetOpt.isPresent()) {
            return buildBudgetResponse(budgetOpt.get(), actualTotalExpense, userId, startDate, endDate);
        } else {
            // If no explicit budget was created, create a virtual baseline response using user's baseline budget if set
            User user = authService.getCurrentUserEntity();
            BigDecimal limit = user.getMonthlyBudgetBaseline() != null && user.getMonthlyBudgetBaseline().compareTo(BigDecimal.ZERO) > 0
                    ? user.getMonthlyBudgetBaseline()
                    : BigDecimal.ZERO;

            BudgetResponse response = new BudgetResponse();
            response.setId(null);
            response.setYearMonth(yearMonth);
            response.setMonthlyLimit(limit);
            response.setTotalSpent(actualTotalExpense);
            response.setRemainingBudget(limit.subtract(actualTotalExpense));
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                double pct = actualTotalExpense.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                response.setPercentageUsed(Math.round(pct * 10.0) / 10.0);
                response.setOverBudget(actualTotalExpense.compareTo(limit) > 0);
            } else {
                response.setPercentageUsed(0.0);
                response.setOverBudget(false);
            }
            response.setNotes("Default baseline estimate. Create a customized monthly budget below.");
            response.setCategoryBudgets(new ArrayList<>());
            return response;
        }
    }

    @Transactional
    public BudgetResponse createOrUpdateBudget(BudgetRequest request) {
        validateBudgetRequest(request);

        User currentUser = authService.getCurrentUserEntity();
        String yearMonth = request.getYearMonth().trim();

        Budget budget = budgetRepository.findByUserIdAndYearMonthWithCategories(currentUser.getId(), yearMonth)
                .orElseGet(() -> {
                    Budget b = new Budget();
                    b.setUser(currentUser);
                    b.setYearMonth(yearMonth);
                    return b;
                });

        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        // Update category allocations
        budget.getCategoryBudgets().clear();
        if (request.getCategoryAllocations() != null) {
            for (BudgetRequest.CategoryAllocationItem item : request.getCategoryAllocations()) {
                if (item.getAllocatedAmount() == null || item.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Category category = categoryService.getCategoryEntityForUser(item.getCategoryId());
                if (category.getType() != TransactionType.EXPENSE) {
                    throw new BadRequestException("Category '" + category.getName() + "' is an income category. Budgets can only be set for expense categories.");
                }

                CategoryBudget cb = new CategoryBudget(budget, category, item.getAllocatedAmount());
                budget.addCategoryBudget(cb);
            }
        }

        Budget saved = budgetRepository.save(budget);

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal actualTotalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                currentUser.getId(), TransactionType.EXPENSE, startDate, endDate
        );
        if (actualTotalExpense == null) actualTotalExpense = BigDecimal.ZERO;

        return buildBudgetResponse(saved, actualTotalExpense, currentUser.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        Long userId = securityUtils.getCurrentUserId();
        List<Budget> budgets = budgetRepository.findAllByUserIdOrderByYearMonthDesc(userId);

        return budgets.stream().map(b -> {
            YearMonth ym = YearMonth.parse(b.getYearMonth());
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();
            BigDecimal actualTotalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.EXPENSE, startDate, endDate
            );
            if (actualTotalExpense == null) actualTotalExpense = BigDecimal.ZERO;
            return buildBudgetResponse(b, actualTotalExpense, userId, startDate, endDate);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteBudget(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));

        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this budget.");
        }

        budgetRepository.delete(budget);
    }

    private BudgetResponse buildBudgetResponse(Budget budget, BigDecimal totalSpent, Long userId, LocalDate startDate, LocalDate endDate) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setYearMonth(budget.getYearMonth());
        response.setMonthlyLimit(budget.getMonthlyLimit());
        response.setTotalSpent(totalSpent);
        response.setRemainingBudget(budget.getMonthlyLimit().subtract(totalSpent));

        if (budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            double pct = totalSpent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            response.setPercentageUsed(Math.round(pct * 10.0) / 10.0);
            response.setOverBudget(totalSpent.compareTo(budget.getMonthlyLimit()) > 0);
        } else {
            response.setPercentageUsed(0.0);
            response.setOverBudget(false);
        }

        response.setNotes(budget.getNotes());
        response.setUpdatedAt(budget.getUpdatedAt());

        // Process category budgets
        List<CategoryBudgetDto> catDtos = new ArrayList<>();
        if (budget.getCategoryBudgets() != null) {
            for (CategoryBudget cb : budget.getCategoryBudgets()) {
                BigDecimal spentInCat = transactionRepository.sumExpenseByCategoryAndDateBetween(
                        userId, cb.getCategory().getId(), startDate, endDate
                );
                if (spentInCat == null) spentInCat = BigDecimal.ZERO;

                BigDecimal allocated = cb.getAllocatedAmount();
                BigDecimal rem = allocated.subtract(spentInCat);
                double pct = 0.0;
                if (allocated.compareTo(BigDecimal.ZERO) > 0) {
                    pct = spentInCat.divide(allocated, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                    pct = Math.round(pct * 10.0) / 10.0;
                }

                CategoryBudgetDto dto = new CategoryBudgetDto(
                        cb.getCategory().getId(),
                        cb.getCategory().getName(),
                        cb.getCategory().getColor(),
                        cb.getCategory().getIcon(),
                        allocated,
                        spentInCat,
                        rem,
                        pct,
                        spentInCat.compareTo(allocated) > 0
                );
                catDtos.add(dto);
            }
        }
        response.setCategoryBudgets(catDtos);

        return response;
    }

    private void validateBudgetRequest(BudgetRequest request) {
        if (request.getYearMonth() == null || !request.getYearMonth().matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new BadRequestException("Invalid Year-Month format. Expected YYYY-MM.");
        }
        if (request.getMonthlyLimit() == null || request.getMonthlyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Monthly budget limit must be greater than zero.");
        }
    }
}
