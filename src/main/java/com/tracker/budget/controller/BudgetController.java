package com.tracker.budget.controller;

import com.tracker.budget.dto.ApiResponse;
import com.tracker.budget.dto.BudgetRequest;
import com.tracker.budget.dto.BudgetResponse;
import com.tracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<BudgetResponse>> getCurrentBudget(@RequestParam(required = false) String month) {
        BudgetResponse response = budgetService.getBudgetForMonth(month);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createOrUpdateBudget(request);
        return ResponseEntity.ok(ApiResponse.ok("Budget saved successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets() {
        List<BudgetResponse> response = budgetService.getAllBudgets();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.ok("Budget deleted successfully.", null));
    }
}
