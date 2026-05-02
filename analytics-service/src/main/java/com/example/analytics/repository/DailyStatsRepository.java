package com.example.analytics.repository;

import com.example.analytics.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO daily_stats (stat_date, action, count) VALUES (:date, :action, 1) " +
                   "ON DUPLICATE KEY UPDATE count = count + 1", nativeQuery = true)
    void upsertCount(@Param("date") LocalDate date, @Param("action") String action);

    List<DailyStats> findByDateGreaterThanEqualOrderByDateDescActionAsc(LocalDate from);
}
