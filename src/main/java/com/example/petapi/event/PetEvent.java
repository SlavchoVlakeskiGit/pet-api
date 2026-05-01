package com.example.petapi.event;

public record PetEvent(Long petId, String petName, String eventType, String occurredAt) {}
