package com.example.petapi.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository repository;

    @Mock
    private PetMapper mapper;

    @Mock
    private PetEventPublisher eventPublisher;

    @Mock
    private PetAuditService auditService;

    @Mock
    private PetAuditRepository auditRepository;

    @InjectMocks
    private PetService service;

    @Test
    void getPetById_returnsPet_whenFound() {
        Pet pet = new Pet(1L, "Milo", "Dog", "Jane", 3);
        when(repository.findById(1L)).thenReturn(Optional.of(pet));
        when(mapper.toResponse(pet)).thenReturn(petResponse(1L, "Milo", "Dog", "Jane", 3));

        PetResponse response = service.getPetById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Milo", response.getName());
        assertEquals("Dog", response.getSpecies());
        assertEquals("Jane", response.getOwnerName());
        assertEquals(3, response.getAge());
    }

    @Test
    void getPetById_throwsPetNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> service.getPetById(99L));
    }

    @Test
    void addPet_savesAndReturnsPet() {
        CreatePetRequest request = new CreatePetRequest();
        request.setName("Milo");
        request.setSpecies("Dog");
        request.setOwnerName("Jane");
        request.setAge(3);

        Pet petEntity = new Pet(null, "Milo", "Dog", "Jane", 3);
        Pet savedPet = new Pet(1L, "Milo", "Dog", "Jane", 3);

        when(mapper.toEntity(request)).thenReturn(petEntity);
        when(repository.savePet(petEntity)).thenReturn(savedPet);
        when(mapper.toResponse(savedPet)).thenReturn(petResponse(1L, "Milo", "Dog", "Jane", 3));

        PetResponse response = service.addPet(request);

        assertEquals(1L, response.getId());
        assertEquals("Milo", response.getName());
    }

    @Test
    void updatePet_updatesOnlyProvidedFields() {
        Pet existingPet = new Pet(1L, "Milo", "Dog", "Jane", 3);
        when(repository.findById(1L)).thenReturn(Optional.of(existingPet));
        when(repository.savePet(existingPet)).thenReturn(existingPet);
        when(mapper.toResponse(existingPet)).thenReturn(petResponse(1L, "Milo", "Dog", "Jane", 5));

        UpdatePetRequest request = new UpdatePetRequest();
        request.setAge(5);

        PetResponse response = service.updatePet(1L, request);

        assertEquals(5, response.getAge());
        assertEquals("Milo", response.getName());
    }

    @Test
    void updatePet_throwsIllegalArgumentException_whenNameIsBlank() {
        Pet existingPet = new Pet(1L, "Milo", "Dog", "Jane", 3);
        when(repository.findById(1L)).thenReturn(Optional.of(existingPet));

        UpdatePetRequest request = new UpdatePetRequest();
        request.setName("   ");

        assertThrows(IllegalArgumentException.class, () -> service.updatePet(1L, request));
    }

    @Test
    void deletePet_throwsPetNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> service.deletePet(99L));
    }

    @Test
    void deletePet_softDeletes_whenPetExists() {
        Pet pet = new Pet(1L, "Milo", "Dog", "Jane", 3);
        when(repository.findById(1L)).thenReturn(Optional.of(pet));

        service.deletePet(1L);

        assertNotNull(pet.getDeletedAt());
        verify(repository).savePet(pet);
    }

    private PetResponse petResponse(Long id, String name, String species, String ownerName, Integer age) {
        PetResponse r = new PetResponse();
        r.setId(id);
        r.setName(name);
        r.setSpecies(species);
        r.setOwnerName(ownerName);
        r.setAge(age);
        return r;
    }
}
