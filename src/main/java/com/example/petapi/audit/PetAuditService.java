package com.example.petapi.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PetAuditService {

    private static final Logger log = LoggerFactory.getLogger(PetAuditService.class);

    private final PetAuditRepository repository;

    public PetAuditService(PetAuditRepository repository) {
        this.repository = repository;
    }

    @Async
    public void log(Long petId, String petName, String action, String performedBy) {
        try {
            repository.save(new PetAuditLog(petId, petName, action, performedBy));
        } catch (Exception e) {
            log.warn("Failed to write audit log for petId={}, action={}: {}", petId, action, e.getMessage());
        }
    }
}
