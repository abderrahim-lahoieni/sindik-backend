package com.sindikbackend.admin.unit.entities;

import com.sindikbackend.admin.building.entities.Building;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "units", indexes = {
        @Index(name = "idx_unit_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_unit_number", columnList = "unit_number"),
        @Index(name = "idx_unit_building", columnList = "building_id")
})
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @NotBlank(message = "Unit number is required")
    @Column(name = "unit_number", nullable = false, length = 50)
    private String unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType type;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(precision = 10, scale = 2)
    private BigDecimal area; // Surface en m²

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @Column(name = "number_of_bathrooms")
    private Integer numberOfBathrooms;

    @Column(name = "has_balcony")
    @Builder.Default
    private Boolean hasBalcony = false;

    @Column(name = "has_parking")
    @Builder.Default
    private Boolean hasParking = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOccupied = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ============================================
    // RELATION AVEC BUILDING
    // ============================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}