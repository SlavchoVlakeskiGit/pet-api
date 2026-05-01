package com.example.petapi.repository;
import com.example.petapi.model.Pet;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface JpaPetRepository extends JpaRepository<Pet, Long>, PetRepository {
    default Pet savePet(Pet pet) {
        return save(pet);
    }
}
