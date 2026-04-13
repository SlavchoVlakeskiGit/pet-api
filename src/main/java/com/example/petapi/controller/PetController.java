package com.example.petapi.controller;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets() {
        return ResponseEntity.ok(service.getAllPets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPetById(id));
    }

    @PostMapping
    public ResponseEntity<PetResponse> addPet(@RequestBody @Valid CreatePetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPet(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                 @RequestBody @Valid UpdatePetRequest request) {
        return ResponseEntity.ok(service.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        service.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
