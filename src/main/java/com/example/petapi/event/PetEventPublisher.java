package com.example.petapi.event;

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

    public void publish(Long petId, String petName, String eventType) {
        PetEvent event = new PetEvent(petId, petName, eventType, LocalDateTime.now().toString());
        kafkaTemplate.send(TOPIC, String.valueOf(petId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for pet id={}", eventType, petId, ex);
                    } else {
                        log.info("Published {} event for pet id={}", eventType, petId);
                    }
                });
    }
}
