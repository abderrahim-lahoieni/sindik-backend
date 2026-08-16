package com.sindikbackend.syndic.contract.repositories;// src/main/java/com/sindikbackend/contract/repositories/ContractRepository.java

import com.sindikbackend.syndic.contract.entities.Contract;
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    Optional<Contract> findByContractNumber(String contractNumber);

    // Contrats d'un résident
    @Query("SELECT c FROM Contract c WHERE c.resident.uuid = :residentUuid")
    List<Contract> findByResidentUuid(@Param("residentUuid") UUID residentUuid);

    // Contrats d'une unité
    @Query("SELECT c FROM Contract c WHERE c.unit.uuid = :unitUuid")
    List<Contract> findByUnitUuid(@Param("unitUuid") UUID unitUuid);

    // Contrats d'un bâtiment
    @Query("SELECT c FROM Contract c WHERE c.unit.building.uuid = :buildingUuid")
    List<Contract> findByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    // Contrats d'une résidence
    @Query("SELECT c FROM Contract c WHERE c.unit.building.residence.uuid = :residenceUuid")
    List<Contract> findByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    // Contrats par statut
    List<Contract> findByStatus(ContractStatus status);

    // Contrats actifs
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.isActive = true")
    List<Contract> findAllActive();

    // Contrats expirant bientôt
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :startDate AND :endDate")
    List<Contract> findExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Contrats expirés
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate < :currentDate")
    List<Contract> findExpired(@Param("currentDate") LocalDate currentDate);

    // Filtres multiples
    @Query("SELECT c FROM Contract c WHERE " +
            "(:residentUuid IS NULL OR c.resident.uuid = :residentUuid) AND " +
            "(:unitUuid IS NULL OR c.unit.uuid = :unitUuid) AND " +
            "(:buildingUuid IS NULL OR c.unit.building.uuid = :buildingUuid) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    List<Contract> findByFilters(
            @Param("residentUuid") UUID residentUuid,
            @Param("unitUuid") UUID unitUuid,
            @Param("buildingUuid") UUID buildingUuid,
            @Param("type") ContractType type,
            @Param("status") ContractStatus status,
            @Param("isActive") Boolean isActive
    );

    // Statistiques
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.unit.building.uuid = :buildingUuid AND c.status = 'ACTIVE'")
    long countActiveByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(@Param("status") ContractStatus status);
}