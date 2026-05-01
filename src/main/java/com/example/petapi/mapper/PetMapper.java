package com.example.petapi.mapper;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.model.Pet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PetMapper {

    PetResponse toResponse(Pet pet);

    Pet toEntity(CreatePetRequest request);
}
