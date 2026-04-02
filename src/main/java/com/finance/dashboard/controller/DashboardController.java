package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse.CategoryTotalResponse;
import com.finance.dashboard.dto.response.MonthlyTrendResponse;
import com.finance.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<MonthlyTrendResponse>> getMonthlyTrends() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyTrends()));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategoryTotals() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCategoryTotals()));
    }
}
