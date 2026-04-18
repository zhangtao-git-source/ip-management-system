package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = dashboardService.getOverview();
        return Result.success(data);
    }

    @GetMapping("/region-stats")
    public Result<Map<String, Object>> regionStats() {
        Map<String, Object> data = dashboardService.getRegionStats();
        return Result.success(data);
    }

    @GetMapping("/recent-allocations")
    public Result<Map<String, Object>> recentAllocations(
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> data = dashboardService.getRecentAllocations(limit);
        return Result.success(data);
    }
}
