package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.dto.response.DashboardSummaryResponse.CategoryTotalResponse;
import com.finance.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService s){this.dashboardService=s;}
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(){return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));}
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<MonthlyTrendResponse>> getTrends(){return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyTrends()));}
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryTotalResponse>>> getCategories(){return ResponseEntity.ok(ApiResponse.success(dashboardService.getCategoryTotals()));}
}
