package com.example.analytics.repository;

import com.example.analytics.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByDateAndAction(LocalDate date, String action);
    List<DailyStats> findByDateGreaterThanEqualOrderByDateDescActionAsc(LocalDate from);
}
