package com.sindikbackend.admin.building.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BuildingResponse {

    private UUID uuid;
    private String name;
    private String address;
    private Integer numberOfFloors;
    private Integer numberOfUnits;
    private String description;
    private Boolean isActive;

    // Informations de la résidence parente
    private UUID residenceUuid;
    private String residenceName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}