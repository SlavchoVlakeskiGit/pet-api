package com.example.analytics.dto;

import java.util.Map;

public record StatsResponse(
        long totalCreated,
        long totalUpdated,
        long totalDeleted,
        long activePets,
        Map<String, Long> bySpecies
) {}
