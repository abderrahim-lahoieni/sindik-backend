package com.sindikbackend.admin.unit.repositories;

import com.sindikbackend.admin.unit.entities.Unit;
import com.sindikbackend.admin.unit.entities.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    Optional<Unit> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("SELECT u FROM Unit u WHERE u.isActive = true ORDER BY u.unitNumber ASC")
    List<Unit> findAllActive();

    @Query("SELECT u FROM Unit u WHERE u.building.uuid = :buildingUuid ORDER BY u.floorNumber ASC, u.unitNumber ASC")
    List<Unit> findByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT u FROM Unit u WHERE u.building.uuid = :buildingUuid AND u.isActive = true ORDER BY u.floorNumber ASC, u.unitNumber ASC")
    List<Unit> findActiveByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT u FROM Unit u WHERE u.building.residence.uuid = :residenceUuid ORDER BY u.building.name ASC, u.unitNumber ASC")
    List<Unit> findByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT u FROM Unit u WHERE u.type = :type AND u.isActive = true")
    List<Unit> findByTypeActive(@Param("type") UnitType type);

    @Query("SELECT u FROM Unit u WHERE u.isOccupied = false AND u.isActive = true")
    List<Unit> findAvailableUnits();

    @Query("SELECT u FROM Unit u WHERE u.isOccupied = true AND u.isActive = true")
    List<Unit> findOccupiedUnits();

    @Query("SELECT u FROM Unit u WHERE LOWER(u.unitNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Unit> searchByUnitNumber(@Param("keyword") String keyword);

    @Query("""
        SELECT u FROM Unit u 
        WHERE (:buildingUuid IS NULL OR u.building.uuid = :buildingUuid) 
        AND (:residenceUuid IS NULL OR u.building.residence.uuid = :residenceUuid)
        AND (:type IS NULL OR u.type = :type)
        AND (:occupied IS NULL OR u.isOccupied = :occupied)
        AND (:active IS NULL OR u.isActive = :active)
        ORDER BY u.building.name ASC, u.floorNumber ASC, u.unitNumber ASC
    """)
    List<Unit> findByFilters(
            @Param("buildingUuid") UUID buildingUuid,
            @Param("residenceUuid") UUID residenceUuid,
            @Param("type") UnitType type,
            @Param("occupied") Boolean occupied,
            @Param("active") Boolean active
    );

    @Query("SELECT COUNT(u) FROM Unit u WHERE u.building.uuid = :buildingUuid")
    long countByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT COUNT(u) FROM Unit u WHERE u.building.uuid = :buildingUuid AND u.isOccupied = false AND u.isActive = true")
    long countAvailableByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    @Query("SELECT COUNT(u) FROM Unit u WHERE u.building.residence.uuid = :residenceUuid")
    long countByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);
}