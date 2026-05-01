package com.example.petapi.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PetAuditRepository extends MongoRepository<PetAuditLog, String> {

    List<PetAuditLog> findByPetIdOrderByOccurredAtDesc(Long petId);
}
