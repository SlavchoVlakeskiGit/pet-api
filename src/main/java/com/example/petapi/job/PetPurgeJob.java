package com.example.petapi.job;

import com.example.petapi.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PetPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(PetPurgeJob.class);

    private final PetService petService;

    public PetPurgeJob(PetService petService) {
        this.petService = petService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void purgeOldSoftDeletedPets() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deleted = petService.purgeSoftDeleted(threshold);
        log.info("Purge job completed: hard-deleted {} pets soft-deleted before {}", deleted, threshold);
    }
}
