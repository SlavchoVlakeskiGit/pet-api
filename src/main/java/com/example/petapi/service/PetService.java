package com.example.petapi.service;

import com.example.petapi.audit.PetAuditLog;
import com.example.petapi.audit.PetAuditRepository;
import com.example.petapi.audit.PetAuditService;
import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.event.PetEventPublisher;
import com.example.petapi.exception.PetNotFoundException;
import com.example.petapi.mapper.PetMapper;
import com.example.petapi.model.Pet;
import com.example.petapi.repository.PetRepository;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetRepository repository;
    private final PetMapper mapper;
    private final PetEventPublisher eventPublisher;
    private final PetAuditService auditService;
    private final PetAuditRepository auditRepository;
    private final Counter petsCreatedCounter;
    private final Counter petsUpdatedCounter;
    private final Counter petsDeletedCounter;

    public PetService(PetRepository repository, PetMapper mapper,
                      PetEventPublisher eventPublisher, PetAuditService auditService,
                      PetAuditRepository auditRepository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.auditRepository = auditRepository;
        this.petsCreatedCounter = Counter.builder("pets.created").description("Total pets created").register(meterRegistry);
        this.petsUpdatedCounter = Counter.builder("pets.updated").description("Total pets updated").register(meterRegistry);
        this.petsDeletedCounter = Counter.builder("pets.deleted").description("Total pets soft-deleted").register(meterRegistry);
    }

    public Page<PetResponse> getAllPets(String species, String ownerName, Pageable pageable) {
        log.info("Fetching pets - species: {}, ownerName: {}, page: {}", species, ownerName, pageable.getPageNumber());
        return repository.findAllActive(species, ownerName, pageable)
                .map(mapper::toResponse);
    }

    @Cacheable(value = "pets", key = "#id")
    @Retry(name = "pet-db")
    public PetResponse getPetById(Long id) {
        log.info("Fetching pet with id: {}", id);
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pet not found with id: {}", id);
                    return new PetNotFoundException(id);
                });
        if (existingPet.getDeletedAt() != null) {
            log.warn("Pet with id: {} has been deleted", id);
            throw new PetNotFoundException(id);
        }
        return mapper.toResponse(existingPet);
    }

    public PetResponse addPet(CreatePetRequest request) {
        log.info("Creating new pet with name: {}", request.getName());
        Pet pet = mapper.toEntity(request);
        PetResponse response = mapper.toResponse(repository.savePet(pet));
        log.info("Pet created with id: {}", response.getId());
        eventPublisher.publish(response.getId(), response.getName(), "CREATED");
        auditService.log(response.getId(), response.getName(), "CREATED", currentUser());
        petsCreatedCounter.increment();
        return response;
    }

    @CacheEvict(value = "pets", key = "#id")
    public PetResponse updatePet(Long id, UpdatePetRequest request) {
        log.info("Updating pet with id: {}", id);
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pet not found with id: {}", id);
                    return new PetNotFoundException(id);
                });

        if (existingPet.getDeletedAt() != null) {
            log.warn("Attempted to update deleted pet with id: {}", id);
            throw new PetNotFoundException(id);
        }

        rejectIfBlank(request.getName(), "name");
        rejectIfBlank(request.getSpecies(), "species");

        if (request.getName() != null) existingPet.setName(request.getName());
        if (request.getSpecies() != null) existingPet.setSpecies(request.getSpecies());
        if (request.getAge() != null) existingPet.setAge(request.getAge());
        if (request.getOwnerName() != null) existingPet.setOwnerName(request.getOwnerName());

        PetResponse response = mapper.toResponse(repository.savePet(existingPet));
        log.info("Pet with id: {} updated successfully", id);
        eventPublisher.publish(id, response.getName(), "UPDATED");
        auditService.log(id, response.getName(), "UPDATED", currentUser());
        petsUpdatedCounter.increment();
        return response;
    }

    @CacheEvict(value = "pets", key = "#id")
    public void deletePet(Long id) {
        log.info("Soft deleting pet with id: {}", id);
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pet not found with id: {}", id);
                    return new PetNotFoundException(id);
                });
        String petName = existingPet.getName();
        existingPet.setDeletedAt(LocalDateTime.now());
        repository.savePet(existingPet);
        log.info("Pet with id: {} soft deleted", id);
        eventPublisher.publish(id, petName, "DELETED");
        auditService.log(id, petName, "DELETED", currentUser());
        petsDeletedCounter.increment();
    }

    public List<PetAuditLog> getAuditLog(Long petId) {
        return auditRepository.findByPetIdOrderByOccurredAtDesc(petId);
    }

    private void rejectIfBlank(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("Pet " + fieldName + " must not be blank");
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}
