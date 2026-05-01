package com.example.petapi.mapper;

import com.example.petapi.dto.CreatePetRequest;
import com.example.petapi.dto.PetResponse;
import com.example.petapi.model.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PetMapper {

    PetMapper INSTANCE = Mappers.getMapper(PetMapper.class);

    PetResponse toResponse(Pet pet);

    Pet toEntity(CreatePetRequest request);
}
