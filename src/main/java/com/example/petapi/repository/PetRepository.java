package com.example.petapi.repository;

import com.example.petapi.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PetRepository {

    List<Pet> findAll();

    Optional<Pet> findById(Long id);

    Pet savePet(Pet pet);

    void deleteById(Long id);

    Page<Pet> findAllActive(String species, String ownerName, Pageable pageable);

    int hardDeleteSoftDeletedBefore(LocalDateTime before);
}
