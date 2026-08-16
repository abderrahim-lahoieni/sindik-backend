package com.sindikbackend.syndic.payment.entities;
import com.sindikbackend.admin.resident.entities.Resident;
import com.sindikbackend.syndic.contract.entities.Contract;
import com.sindikbackend.syndic.payment.enums.PaymentMethod;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_payment_contract", columnList = "contract_id"),
                @Index(name = "idx_payment_resident", columnList = "resident_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_due_date", columnList = "due_date"),
                @Index(name = "idx_payment_reference", columnList = "payment_reference")
        })
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", unique = true, length = 50)
    private String paymentReference;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "late_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "receipt_url")
    private String receiptUrl; // URL du reçu/facture

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

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

        if (paymentReference == null) {
            paymentReference = generatePaymentReference();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generatePaymentReference() {
        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PAY-" + year + month + "-" + randomPart;
    }

    // Méthode utilitaire pour calculer le montant restant
    public BigDecimal getRemainingAmount() {
        return amount.subtract(amountPaid).add(lateFee).subtract(discount);
    }

    // Vérifier si le paiement est en retard
    public boolean isLate() {
        if (status == PaymentStatus.PAID || status == PaymentStatus.CANCELLED) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }
}