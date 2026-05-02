package com.example.analytics.listener;

import com.example.analytics.event.PetEvent;
import com.example.analytics.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class PetEventListener {

    private static final Logger log = LoggerFactory.getLogger(PetEventListener.class);

    private final StatsService statsService;

    public PetEventListener(StatsService statsService) {
        this.statsService = statsService;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "pet-events", groupId = "analytics-service")
    public void handle(PetEvent event) {
        log.info("Processing event: type={}, pet={} (id={})", event.eventType(), event.petName(), event.petId());
        statsService.recordEvent(event);
    }

    @DltHandler
    public void handleDlt(PetEvent event) {
        log.error("DLT: analytics failed to process event after all retries — type={}, pet={} (id={})",
                event.eventType(), event.petName(), event.petId());
    }
}
