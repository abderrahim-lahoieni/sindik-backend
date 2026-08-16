package com.sindikbackend.admin.resident.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ResidentUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;

    private String emergencyContact;

    private String emergencyPhone;

    private UUID unitUuid;

    private UUID userUuid;

    private LocalDate moveInDate;

    private LocalDate moveOutDate;

    private Boolean isOwner;

    private Boolean isPrimaryResident;

    private String notes;

    private Boolean isActive;
}