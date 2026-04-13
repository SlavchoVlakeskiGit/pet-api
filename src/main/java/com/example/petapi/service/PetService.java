package com.example.petapi.service;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.exception.PetNotFoundException;
import com.example.petapi.model.Pet;
import com.example.petapi.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    private final PetRepository repository;

    public PetService(PetRepository repository) {
        this.repository = repository;
    }

    public List<PetResponse> getAllPets() {
        return repository.findAll().stream()
                .map(this::buildResponse)
                .toList();
    }

    public PetResponse getPetById(Long id) {
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        return buildResponse(existingPet);
    }

    public PetResponse addPet(CreatePetRequest request) {
        Pet pet = new Pet(null, request.getName(), request.getSpecies(),
                request.getOwnerName(), request.getAge());
        return buildResponse(repository.savePet(pet));
    }

    public PetResponse updatePet(Long id, UpdatePetRequest request) {
        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));

        // null means the client didn't send this field — skip it
        // blank means they sent an empty string — that's an error
        rejectIfBlank(request.getName(), "name");
        rejectIfBlank(request.getSpecies(), "species");

        if (request.getName() != null) {
            existingPet.setName(request.getName());
        }

        if (request.getSpecies() != null) {
            existingPet.setSpecies(request.getSpecies());
        }

        if (request.getAge() != null) {
            existingPet.setAge(request.getAge());
        }

        if (request.getOwnerName() != null) {
            existingPet.setOwnerName(request.getOwnerName());
        }

        return buildResponse(repository.savePet(existingPet));
    }

    public void deletePet(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        repository.deleteById(id);
    }

    private void rejectIfBlank(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("Pet " + fieldName + " must not be blank");
        }
    }

    private PetResponse buildResponse(Pet pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getSpecies(),
                pet.getAge(), pet.getOwnerName());
    }
}
