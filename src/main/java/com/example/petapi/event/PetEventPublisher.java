package com.example.petapi.event;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PetEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PetEventPublisher.class);
    static final String TOPIC = "pet-events";

    private final KafkaTemplate<String, PetEvent> kafkaTemplate;

    public PetEventPublisher(KafkaTemplate<String, PetEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "kafka-publisher", fallbackMethod = "publishFallback")
    @Bulkhead(name = "kafka-publisher", fallbackMethod = "publishFallback")
    public void publish(Long petId, String petName, String species, String eventType) {
        PetEvent event = new PetEvent(petId, petName, eventType, LocalDateTime.now().toString(), species);
        kafkaTemplate.send(TOPIC, String.valueOf(petId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for pet id={}", eventType, petId, ex);
                    } else {
                        log.info("Published {} event for pet id={}", eventType, petId);
                    }
                });
    }

    void publishFallback(Long petId, String petName, String species, String eventType, Exception ex) {
        log.warn("Circuit breaker open — skipping {} event for pet id={}: {}", eventType, petId, ex.getMessage());
    }
}
