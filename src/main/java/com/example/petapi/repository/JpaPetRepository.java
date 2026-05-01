package com.example.petapi.repository;

import com.example.petapi.model.Pet;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
@Primary
public interface JpaPetRepository extends JpaRepository<Pet, Long>, PetRepository {

    default Pet savePet(Pet pet) {
        return save(pet);
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM Pet p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :before")
    int hardDeleteSoftDeletedBefore(@Param("before") LocalDateTime before);

    @Query("SELECT p FROM Pet p WHERE p.deletedAt IS NULL " +
           "AND (:species IS NULL OR p.species = :species) " +
           "AND (:ownerName IS NULL OR p.ownerName = :ownerName)")
    Page<Pet> findAllActive(
            @Param("species") String species,
            @Param("ownerName") String ownerName,
            Pageable pageable);
}
