package com.sindikbackend.admin.building.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class BuildingUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String address;

    private UUID residenceUuid;

    @Min(value = 1, message = "Number of floors must be at least 1")
    private Integer numberOfFloors;

    @Min(value = 0, message = "Number of units cannot be negative")
    private Integer numberOfUnits;

    private String description;

    private Boolean isActive;
}