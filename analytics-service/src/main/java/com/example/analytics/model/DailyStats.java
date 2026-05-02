package com.example.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stat_date", "action"})
})
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Long count = 0L;

    public DailyStats() {}

    public DailyStats(LocalDate date, String action, Long count) {
        this.date = date;
        this.action = action;
        this.count = count;
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
