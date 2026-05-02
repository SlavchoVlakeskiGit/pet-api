package com.example.analytics.controller;

import com.example.analytics.dto.DailyStatsEntry;
import com.example.analytics.dto.StatsResponse;
import com.example.analytics.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/stats")
@Tag(name = "Statistics")
@Validated
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @Operation(summary = "Overall pet lifecycle counts and active pet total")
    public StatsResponse getOverallStats() {
        return statsService.getOverallStats();
    }

    @GetMapping("/daily")
    @Operation(summary = "Daily breakdown of events for the past N days (max 90)")
    public List<DailyStatsEntry> getDailyStats(
            @RequestParam(defaultValue = "30") @Min(1) @Max(90) int days) {
        return statsService.getDailyStats(days);
    }
}
