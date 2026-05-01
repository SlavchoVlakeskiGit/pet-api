package com.example.petapi.repository;

import com.example.petapi.model.Pet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// temporary in-memory storage, no database needed for now
@Repository
public class InMemoryPetRepository implements PetRepository {

    private final Map<Long, Pet> petStore = new ConcurrentHashMap<>(); // keyed by pet id
    private final AtomicLong idCounter = new AtomicLong(1); // starts at 1, not 0

    @Override
    public List<Pet> findAll() {
        return new ArrayList<>(petStore.values());
    }

    @Override
    public Optional<Pet> findById(Long id) {
        return Optional.ofNullable(petStore.get(id));
    }

    @Override
    public Pet savePet(Pet pet) {
        if (pet.getId() == null) {
            pet.setId(idCounter.getAndIncrement());
        }
        petStore.put(pet.getId(), pet);
        return pet;
    }

    @Override
    public void deleteById(Long id) {
        petStore.remove(id);
    }

    @Override
    public org.springframework.data.domain.Page<com.example.petapi.model.Pet> findAllActive(
            String species, String ownerName, org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("Not supported in-memory");
    }
}
