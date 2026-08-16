package com.sindikbackend.admin.residence.dtos.responses;

import com.sindikbackend.admin.residence.entities.ResidenceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResidenceResponse {

    private UUID uuid;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private ResidenceType type;
    private Integer numberOfBuildings;
    private Integer numberOfUnits;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}