package com.example.analytics.dto;

import java.time.LocalDate;

public record DailyStatsEntry(LocalDate date, String action, long count) {}
