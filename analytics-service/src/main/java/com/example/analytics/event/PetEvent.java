package com.example.analytics.event;

public record PetEvent(Long petId, String petName, String eventType, String occurredAt, String species) {}
