package com.example.petapi.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pet_audit_logs")
public class PetAuditLog {

    @Id
    private String id;

    @Indexed
    private Long petId;

    private String petName;
    private String action;
    private String performedBy;

    @Indexed(expireAfterSeconds = 31536000) // auto-expire after 1 year
    private LocalDateTime occurredAt;

    public PetAuditLog(Long petId, String petName, String action, String performedBy) {
        this.petId = petId;
        this.petName = petName;
        this.action = action;
        this.performedBy = performedBy;
        this.occurredAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public Long getPetId() { return petId; }
    public String getPetName() { return petName; }
    public String getAction() { return action; }
    public String getPerformedBy() { return performedBy; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
