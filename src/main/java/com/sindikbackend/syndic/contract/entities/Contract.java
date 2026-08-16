package com.sindikbackend.syndic.contract.entities;// src/main/java/com/sindikbackend/contract/entities/Contract.java


import com.sindikbackend.admin.resident.entities.Resident;
import com.sindikbackend.admin.unit.entities.Unit;
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.payment.enums.PaymentFrequency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts",
        indexes = {
                @Index(name = "idx_contract_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_contract_resident", columnList = "resident_id"),
                @Index(name = "idx_contract_unit", columnList = "unit_id"),
                @Index(name = "idx_contract_status", columnList = "status"),
                @Index(name = "idx_contract_dates", columnList = "start_date, end_date")
        })
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "monthly_rent", precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_paid")
    @Builder.Default
    private Boolean depositPaid = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", length = 20)
    private PaymentFrequency paymentFrequency;

    @Column(name = "payment_day")
    private Integer paymentDay; // Jour du mois pour le paiement (1-31)

    @Column(name = "contract_number", unique = true, length = 50)
    private String contractNumber;

    @Column(columnDefinition = "TEXT")
    private String terms; // Termes et conditions

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "renewal_notice_days")
    private Integer renewalNoticeDays; // Nombre de jours avant l'expiration pour rappel

    @Column(name = "auto_renewable")
    @Builder.Default
    private Boolean autoRenewable = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Générer numéro de contrat si non fourni
        if (contractNumber == null) {
            contractNumber = generateContractNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateContractNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CTR-" + year + "-" + randomPart;
    }
}