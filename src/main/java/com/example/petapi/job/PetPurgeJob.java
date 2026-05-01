package com.example.petapi.job;

import com.example.petapi.repository.JpaPetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PetPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(PetPurgeJob.class);

    private final JpaPetRepository repository;

    public PetPurgeJob(JpaPetRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void purgeOldSoftDeletedPets() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deleted = repository.hardDeleteSoftDeletedBefore(threshold);
        log.info("Purge job: hard-deleted {} pets soft-deleted before {}", deleted, threshold);
    }
}
