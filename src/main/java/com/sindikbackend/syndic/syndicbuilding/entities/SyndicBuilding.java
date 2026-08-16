package com.sindikbackend.syndic.syndicbuilding.entities;// entities/SyndicBuilding.java

import com.sindikbackend.admin.building.entities.Building;
import com.sindikbackend.auth.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "syndic_buildings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"syndic_id", "building_id"}),
        indexes = {
                @Index(name = "idx_syndic_building_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_syndic_id", columnList = "syndic_id"),
                @Index(name = "idx_building_id", columnList = "building_id")
        })
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class SyndicBuilding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syndic_id", nullable = false)
    private User syndic; // Utilisateur avec role SYNDIC

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by") // UUID de l'admin qui a fait l'assignation
    private UUID assignedBy;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        assignedAt = LocalDateTime.now();
    }
}