package com.sindikbackend.admin.unit.dtos.responses;

import com.sindikbackend.admin.unit.entities.UnitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UnitResponse {

    private UUID uuid;
    private String unitNumber;
    private UnitType type;
    private Integer floorNumber;
    private BigDecimal area;
    private Integer numberOfRooms;
    private Integer numberOfBathrooms;
    private Boolean hasBalcony;
    private Boolean hasParking;
    private String description;
    private Boolean isOccupied;
    private Boolean isActive;

    // Informations du bâtiment parent
    private UUID buildingUuid;
    private String buildingName;

    // Informations de la résidence
    private UUID residenceUuid;
    private String residenceName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}