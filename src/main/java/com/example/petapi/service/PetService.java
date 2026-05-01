package com.example.petapi.service;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.exception.PetNotFoundException;
import com.example.petapi.mapper.PetMapper;
import com.example.petapi.model.Pet;
import com.example.petapi.repository.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetRepository repository;
    private final PetMapper mapper;

    public PetService(PetRepository repository, PetMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<PetResponse> getAllPets(String species, String ownerName, Pageable pageable) {
        log.info("Fetching pets - species: {}, ownerName: {}, page: {}", species, ownerName, pageable.getPageNumber());
        return repository.findAllActive(species, ownerName, pageable)
                .map(mapper::toResponse);
    }

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
        return response;
    }

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
        return response;
    }

    public void deletePet(Long id) {
        log.info("Soft deleting pet with id: {}", id);
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pet not found with id: {}", id);
                    return new PetNotFoundException(id);
                });
        existingPet.setDeletedAt(LocalDateTime.now());
        repository.savePet(existingPet);
        log.info("Pet with id: {} soft deleted", id);
    }

    private void rejectIfBlank(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("Pet " + fieldName + " must not be blank");
        }
    }
}
