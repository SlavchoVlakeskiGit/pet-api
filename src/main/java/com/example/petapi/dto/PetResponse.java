package com.example.petapi.dto;

public class PetResponse {

    private Long id;
    private String name;
    private String species;
    private Integer age;
    private String ownerName;

    public PetResponse(Long id, String name, String species, Integer age, String ownerName) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.age = age;
        this.ownerName = ownerName;
    }

    public PetResponse() {}

    public Long getId() { return id; }
    public String getName() {return name; }
    public String getSpecies() { return species; }
    public Integer getAge() { return age; }
    public String getOwnerName() { return ownerName; }
}
