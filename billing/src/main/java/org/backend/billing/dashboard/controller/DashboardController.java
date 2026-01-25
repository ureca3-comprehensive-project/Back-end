package org.backend.billing.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.backend.billing.dashboard.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/recentFailures")
    public ResponseEntity<List<Map<String, Object>>> getRecentFailures() {
        return ResponseEntity.ok(dashboardService.getRecentFailures());
    }
}