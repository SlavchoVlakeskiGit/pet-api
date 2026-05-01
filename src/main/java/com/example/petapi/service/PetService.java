package com.example.petapi.service;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.exception.PetNotFoundException;
import com.example.petapi.mapper.PetMapper;
import com.example.petapi.model.Pet;
import com.example.petapi.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetService {

    private final PetRepository repository;
    private final PetMapper mapper;

    public PetService(PetRepository repository, PetMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PetResponse> getAllPets() {
        return repository.findAll().stream()
                .filter(pet -> pet.getDeletedAt() == null)
                .map(mapper::toResponse)
                .toList();
    }

    public PetResponse getPetById(Long id) {
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        if (existingPet.getDeletedAt() != null) {
            throw new PetNotFoundException(id);
        }
        return mapper.toResponse(existingPet);
    }

    public PetResponse addPet(CreatePetRequest request) {
        Pet pet = mapper.toEntity(request);
        return mapper.toResponse(repository.savePet(pet));
    }

    public PetResponse updatePet(Long id, UpdatePetRequest request) {
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));

        if (existingPet.getDeletedAt() != null) {
            throw new PetNotFoundException(id);
        }

        rejectIfBlank(request.getName(), "name");
        rejectIfBlank(request.getSpecies(), "species");

        if (request.getName() != null) existingPet.setName(request.getName());
        if (request.getSpecies() != null) existingPet.setSpecies(request.getSpecies());
        if (request.getAge() != null) existingPet.setAge(request.getAge());
        if (request.getOwnerName() != null) existingPet.setOwnerName(request.getOwnerName());

        return mapper.toResponse(repository.savePet(existingPet));
    }

    public void deletePet(Long id) {
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        existingPet.setDeletedAt(LocalDateTime.now());
        repository.savePet(existingPet);
    }

    private void rejectIfBlank(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("Pet " + fieldName + " must not be blank");
        }
    }
}
