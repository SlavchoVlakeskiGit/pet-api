package com.example.petapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdatePetRequest {

    @Size(max = 100, message = "Pet name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Species must not exceed 100 characters")
    private String species;

    @Min(value = 0, message = "Pet age must be zero or greater")
    private Integer age;

    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
