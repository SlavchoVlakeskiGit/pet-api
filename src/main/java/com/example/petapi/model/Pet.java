package com.example.petapi.model;

public class Pet {

    private Long id;
    private String name;
    private String species;
    private String ownerName;
    private Integer age;

    public Pet() {}

    public Pet(Long id, String name, String species, String ownerName, Integer age) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.ownerName = ownerName;
        this.age = age;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    @Override
    public String toString() {
        return "Pet{id=" + id + ", name='" + name + "', species='" + species + "', ownerName='" + ownerName + "', age=" + age + "}";
    }
}