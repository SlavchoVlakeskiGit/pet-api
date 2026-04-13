package com.example.petapi.repository;

import com.example.petapi.model.Pet;

import java.util.List;
import java.util.Optional;

// defines what the storage layer must do
public interface PetRepository {

    List<Pet> findAll();

    Optional<Pet> findById(Long id);

    Pet savePet(Pet pet);

    void deleteById(Long id);
}
