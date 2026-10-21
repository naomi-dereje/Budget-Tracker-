package com.tracker.budget.controller;

import com.tracker.budget.dto.ApiResponse;
import com.tracker.budget.dto.DashboardSummaryResponse;
import com.tracker.budget.dto.ReportSummaryResponse;
import com.tracker.budget.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(required = false) String month) {
        DashboardSummaryResponse response = reportService.getDashboardSummary(month);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getAnalyticsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportSummaryResponse response = reportService.generateReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
