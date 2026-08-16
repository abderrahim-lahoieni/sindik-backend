package com.sindikbackend.admin.unit.dtos.requests;

import com.sindikbackend.admin.unit.entities.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UnitCreateRequest {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    @NotNull(message = "Building UUID is required")
    private UUID buildingUuid;

    @NotNull(message = "Type is required")
    private UnitType type;

    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floorNumber;

    private BigDecimal area;

    @Min(value = 0, message = "Number of rooms cannot be negative")
    private Integer numberOfRooms;

    @Min(value = 0, message = "Number of bathrooms cannot be negative")
    private Integer numberOfBathrooms;

    private Boolean hasBalcony = false;

    private Boolean hasParking = false;

    private String description;

    private Boolean isOccupied = false;

    private Boolean isActive = true;
}