package com.sindikbackend.admin.resident.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ResidentCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;

    private String emergencyContact;

    private String emergencyPhone;

    @NotNull(message = "Unit UUID is required")
    private UUID unitUuid;

    private UUID userUuid; // Optionnel: lier à un compte utilisateur

    private LocalDate moveInDate;

    private LocalDate moveOutDate;

    private Boolean isOwner = false;

    private Boolean isPrimaryResident = true;

    private String notes;

    private Boolean isActive = true;
}