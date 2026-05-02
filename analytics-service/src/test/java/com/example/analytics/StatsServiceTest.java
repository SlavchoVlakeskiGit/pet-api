package com.example.analytics;

import com.example.analytics.event.PetEvent;
import com.example.analytics.model.DailyStats;
import com.example.analytics.repository.DailyStatsRepository;
import com.example.analytics.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock private DailyStatsRepository dailyStatsRepository;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private SetOperations<String, String> setOps;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForSet()).thenReturn(setOps);
        statsService = new StatsService(dailyStatsRepository, redis);
    }

    @Test
    void recordEvent_incrementsRedisCounter() {
        PetEvent event = new PetEvent(1L, "Rex", "CREATED", "2024-01-01T00:00:00", "Dog");
        when(dailyStatsRepository.findByDateAndAction(any(), eq("CREATED"))).thenReturn(Optional.empty());
        when(dailyStatsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        statsService.recordEvent(event);

        verify(valueOps).increment("stats:action:CREATED");
        verify(valueOps).increment("stats:species:Dog:CREATED");
        verify(setOps).add("stats:species:known", "Dog");
    }

    @Test
    void recordEvent_updatesExistingDailyStats() {
        PetEvent event = new PetEvent(2L, "Luna", "UPDATED", "2024-01-01T00:00:00", null);
        DailyStats existing = new DailyStats(LocalDate.now(), "UPDATED", 5L);
        when(dailyStatsRepository.findByDateAndAction(any(), eq("UPDATED"))).thenReturn(Optional.of(existing));
        when(dailyStatsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        statsService.recordEvent(event);

        ArgumentCaptor<DailyStats> captor = ArgumentCaptor.forClass(DailyStats.class);
        verify(dailyStatsRepository).save(captor.capture());
        assertEquals(6L, captor.getValue().getCount());
    }

    @Test
    void recordEvent_createsNewDailyStatsWhenNoneExist() {
        PetEvent event = new PetEvent(3L, "Milo", "DELETED", "2024-01-01T00:00:00", "Cat");
        when(dailyStatsRepository.findByDateAndAction(any(), eq("DELETED"))).thenReturn(Optional.empty());
        when(dailyStatsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        statsService.recordEvent(event);

        ArgumentCaptor<DailyStats> captor = ArgumentCaptor.forClass(DailyStats.class);
        verify(dailyStatsRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getCount());
        assertEquals("DELETED", captor.getValue().getAction());
    }
}
