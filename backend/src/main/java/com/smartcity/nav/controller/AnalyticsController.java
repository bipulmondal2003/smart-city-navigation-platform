package com.smartcity.nav.controller;

import com.smartcity.nav.dto.AnalyticsSummaryDTO;
import com.smartcity.nav.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /** GET /api/analytics - public read-only summary for the dashboard. */
    @GetMapping
    public AnalyticsSummaryDTO getAnalytics() {
        return analyticsService.getSummary();
    }
}
