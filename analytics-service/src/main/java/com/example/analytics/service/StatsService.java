package com.example.analytics.service;

import com.example.analytics.dto.DailyStatsEntry;
import com.example.analytics.dto.StatsResponse;
import com.example.analytics.event.PetEvent;
import com.example.analytics.model.DailyStats;
import com.example.analytics.repository.DailyStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private static final String KEY_ACTION = "stats:action:";
    private static final String KEY_SPECIES = "stats:species:";
    private static final String KEY_SPECIES_KNOWN = "stats:species:known";

    private final DailyStatsRepository dailyStatsRepository;
    private final StringRedisTemplate redis;

    public StatsService(DailyStatsRepository dailyStatsRepository, StringRedisTemplate redis) {
        this.dailyStatsRepository = dailyStatsRepository;
        this.redis = redis;
    }

    @Transactional
    public void recordEvent(PetEvent event) {
        redis.opsForValue().increment(KEY_ACTION + event.eventType());

        if (event.species() != null && !event.species().isBlank()) {
            redis.opsForSet().add(KEY_SPECIES_KNOWN, event.species());
            redis.opsForValue().increment(KEY_SPECIES + event.species() + ":" + event.eventType());
        }

        LocalDate today = LocalDate.now();
        DailyStats entry = dailyStatsRepository.findByDateAndAction(today, event.eventType())
                .orElseGet(() -> new DailyStats(today, event.eventType(), 0L));
        entry.setCount(entry.getCount() + 1);
        dailyStatsRepository.save(entry);

        log.debug("Recorded {} event — daily totals updated", event.eventType());
    }

    @Transactional(readOnly = true)
    public StatsResponse getOverallStats() {
        long created = getLong(KEY_ACTION + "CREATED");
        long updated = getLong(KEY_ACTION + "UPDATED");
        long deleted = getLong(KEY_ACTION + "DELETED");

        Map<String, Long> bySpecies = getSpeciesBreakdown();

        return new StatsResponse(created, updated, deleted, created - deleted, bySpecies);
    }

    @Transactional(readOnly = true)
    public List<DailyStatsEntry> getDailyStats(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);
        return dailyStatsRepository.findByDateGreaterThanEqualOrderByDateDescActionAsc(from)
                .stream()
                .map(s -> new DailyStatsEntry(s.getDate(), s.getAction(), s.getCount()))
                .collect(Collectors.toList());
    }

    private Map<String, Long> getSpeciesBreakdown() {
        Set<String> knownSpecies = redis.opsForSet().members(KEY_SPECIES_KNOWN);
        if (knownSpecies == null || knownSpecies.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> result = new LinkedHashMap<>();
        knownSpecies.stream().sorted().forEach(species ->
                result.put(species, getLong(KEY_SPECIES + species + ":CREATED"))
        );
        return result;
    }

    private long getLong(String key) {
        String val = redis.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }
}
