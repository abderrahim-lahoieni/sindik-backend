package com.sindikbackend.admin.resident.repositories;

import com.sindikbackend.admin.resident.entities.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    Optional<Resident> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("SELECT r FROM Resident r WHERE r.isActive = true ORDER BY r.fullName ASC")
    List<Resident> findAllActive();

    @Query("SELECT r FROM Resident r WHERE r.unit.uuid = :unitUuid ORDER BY r.isPrimaryResident DESC, r.fullName ASC")
    List<Resident> findByUnitUuid(@Param("unitUuid") UUID unitUuid);

    @Query("SELECT r FROM Resident r WHERE r.unit.building.uuid = :buildingUuid ORDER BY r.fullName ASC")
    List<Resident> findByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT r FROM Resident r WHERE r.unit.building.residence.uuid = :residenceUuid ORDER BY r.fullName ASC")
    List<Resident> findByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT r FROM Resident r WHERE r.user.uuid = :userUuid")
    Optional<Resident> findByUserUuid(@Param("userUuid") UUID userUuid);

    List<Resident> findAllByFullNameIgnoreCaseAndUserIsNull(String fullName);

    @Query("SELECT r FROM Resident r WHERE r.isOwner = true AND r.isActive = true")
    List<Resident> findOwners();

    @Query("SELECT r FROM Resident r WHERE r.isPrimaryResident = true AND r.isActive = true")
    List<Resident> findPrimaryResidents();

    @Query("SELECT r FROM Resident r WHERE LOWER(r.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Resident> searchByNameOrPhone(@Param("keyword") String keyword);

    @Query("""
        SELECT r FROM Resident r 
        WHERE (:unitUuid IS NULL OR r.unit.uuid = :unitUuid) 
        AND (:buildingUuid IS NULL OR r.unit.building.uuid = :buildingUuid)
        AND (:residenceUuid IS NULL OR r.unit.building.residence.uuid = :residenceUuid)
        AND (:isOwner IS NULL OR r.isOwner = :isOwner)
        AND (:isPrimaryResident IS NULL OR r.isPrimaryResident = :isPrimaryResident)
        AND (:active IS NULL OR r.isActive = :active)
        ORDER BY r.fullName ASC
    """)
    List<Resident> findByFilters(
            @Param("unitUuid") UUID unitUuid,
            @Param("buildingUuid") UUID buildingUuid,
            @Param("residenceUuid") UUID residenceUuid,
            @Param("isOwner") Boolean isOwner,
            @Param("isPrimaryResident") Boolean isPrimaryResident,
            @Param("active") Boolean active
    );

    @Query("SELECT COUNT(r) FROM Resident r WHERE r.unit.uuid = :unitUuid AND r.isActive = true")
    long countByUnitUuid(@Param("unitUuid") UUID unitUuid);

    @Query("SELECT COUNT(r) FROM Resident r WHERE r.unit.building.uuid = :buildingUuid AND r.isActive = true")
    long countByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT COUNT(r) FROM Resident r WHERE r.unit.building.residence.uuid = :residenceUuid AND r.isActive = true")
    long countByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT COUNT(r) FROM Resident r WHERE r.isOwner = true AND r.isActive = true")
    long countOwners();
}
