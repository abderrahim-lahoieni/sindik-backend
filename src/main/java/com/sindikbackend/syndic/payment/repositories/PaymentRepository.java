package com.sindikbackend.syndic.payment.repositories;// src/main/java/com/sindikbackend/payment/repositories/PaymentRepository.java
import com.sindikbackend.syndic.payment.entities.Payment;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    Optional<Payment> findByPaymentReference(String paymentReference);

    // Paiements d'un contrat
    @Query("SELECT p FROM Payment p WHERE p.contract.uuid = :contractUuid ORDER BY p.dueDate DESC")
    List<Payment> findByContractUuid(@Param("contractUuid") UUID contractUuid);

    // Paiements d'un résident
    @Query("SELECT p FROM Payment p WHERE p.resident.uuid = :residentUuid ORDER BY p.dueDate DESC")
    List<Payment> findByResidentUuid(@Param("residentUuid") UUID residentUuid);

    // Paiements d'un bâtiment
    @Query("SELECT p FROM Payment p WHERE p.contract.unit.building.uuid = :buildingUuid ORDER BY p.dueDate DESC")
    List<Payment> findByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    // Paiements par statut
    List<Payment> findByStatus(PaymentStatus status);

    // Paiements en retard
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PARTIAL', 'OVERDUE') AND p.dueDate < :currentDate")
    List<Payment> findOverdue(@Param("currentDate") LocalDate currentDate);

    // Paiements à venir (dans les N jours)
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.dueDate BETWEEN :startDate AND :endDate")
    List<Payment> findUpcomingBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Paiements d'une période
    @Query("SELECT p FROM Payment p WHERE p.dueDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Filtres multiples
    @Query("SELECT p FROM Payment p WHERE " +
            "(:contractUuid IS NULL OR p.contract.uuid = :contractUuid) AND " +
            "(:residentUuid IS NULL OR p.resident.uuid = :residentUuid) AND " +
            "(:buildingUuid IS NULL OR p.contract.unit.building.uuid = :buildingUuid) AND " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDate IS NULL OR p.dueDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.dueDate <= :endDate)")
    List<Payment> findByFilters(
            @Param("contractUuid") UUID contractUuid,
            @Param("residentUuid") UUID residentUuid,
            @Param("buildingUuid") UUID buildingUuid,
            @Param("type") PaymentType type,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Statistiques financières
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.contract.unit.building.uuid = :buildingUuid AND p.status = 'PAID'")
    BigDecimal sumPaidByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.contract.unit.building.uuid = :buildingUuid AND p.status IN ('PENDING', 'OVERDUE', 'PARTIAL')")
    BigDecimal sumPendingByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumPaidInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'OVERDUE'")
    long countOverdue();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.contract.unit.building.uuid = :buildingUuid AND p.status = 'OVERDUE'")
    long countOverdueByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);
}