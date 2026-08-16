package com.sindikbackend.admin.resident.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResidentResponse {

    private UUID uuid;
    private String fullName;
    private String phoneNumber;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private Boolean isOwner;
    private Boolean isPrimaryResident;
    private String notes;
    private Boolean isActive;

    // Informations du compte utilisateur (si lié)
    private UUID userUuid;
    private String userEmail;

    // Informations de l'unité
    private UUID unitUuid;
    private String unitNumber;

    // Informations du bâtiment
    private UUID buildingUuid;
    private String buildingName;

    // Informations de la résidence
    private UUID residenceUuid;
    private String residenceName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}