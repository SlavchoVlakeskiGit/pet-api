package com.example.petapi.controller;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.dto.UpdatePetRequest;
import com.example.petapi.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@Tag(name = "Pets", description = "Operations for managing pets")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all pets")
    @ApiResponse(responseCode = "200", description = "List of all pets")
    public ResponseEntity<List<PetResponse>> getAllPets() {
        return ResponseEntity.ok(service.getAllPets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a pet by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet found"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPetById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new pet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<PetResponse> addPet(@RequestBody @Valid CreatePetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPet(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a pet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                 @RequestBody @Valid UpdatePetRequest request) {
        return ResponseEntity.ok(service.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pet")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet deleted"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        service.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
