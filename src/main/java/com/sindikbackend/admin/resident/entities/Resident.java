package com.sindikbackend.admin.resident.entities;

import com.sindikbackend.auth.entities.User;
import com.sindikbackend.admin.unit.entities.Unit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "residents", indexes = {
        @Index(name = "idx_resident_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_resident_user", columnList = "user_id"),
        @Index(name = "idx_resident_unit", columnList = "unit_id"),
        @Index(name = "idx_resident_phone", columnList = "phone_number")
})
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "move_out_date")
    private LocalDate moveOutDate;

    @Column(name = "is_owner")
    @Builder.Default
    private Boolean isOwner = false;

    @Column(name = "is_primary_resident")
    @Builder.Default
    private Boolean isPrimaryResident = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ============================================
    // RELATION AVEC USER (optionnelle)
    // ============================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ============================================
    // RELATION AVEC UNIT
    // ============================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

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