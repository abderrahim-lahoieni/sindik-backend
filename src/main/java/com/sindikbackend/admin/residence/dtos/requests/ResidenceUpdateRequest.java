package com.sindikbackend.admin.residence.dtos.requests;

import com.sindikbackend.admin.residence.entities.ResidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ResidenceUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String address;

    private String city;

    private String postalCode;

    private ResidenceType type;

    @Min(value = 1, message = "Number of buildings must be at least 1")
    private Integer numberOfBuildings;

    @Min(value = 0, message = "Number of units cannot be negative")
    private Integer numberOfUnits;

    private String description;

    private Boolean isActive;
}