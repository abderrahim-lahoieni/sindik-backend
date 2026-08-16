package com.sindikbackend.admin.building.repositories;

import com.sindikbackend.admin.building.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    Optional<Building> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("SELECT b FROM Building b WHERE b.isActive = true ORDER BY b.createdAt DESC")
    List<Building> findAllActive();

    @Query("SELECT b FROM Building b WHERE b.residence.uuid = :residenceUuid ORDER BY b.name ASC")
    List<Building> findByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT b FROM Building b WHERE b.residence.uuid = :residenceUuid AND b.isActive = true ORDER BY b.name ASC")
    List<Building> findActiveByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT b FROM Building b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Building> searchByName(@Param("keyword") String keyword);

    @Query("""
        SELECT b FROM Building b 
        WHERE (:residenceUuid IS NULL OR b.residence.uuid = :residenceUuid) 
        AND (:active IS NULL OR b.isActive = :active)
        ORDER BY b.createdAt DESC
    """)
    List<Building> findByFilters(
            @Param("residenceUuid") UUID residenceUuid,
            @Param("active") Boolean active
    );

    @Query("SELECT COUNT(b) FROM Building b WHERE b.residence.uuid = :residenceUuid")
    long countByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);

    @Query("SELECT COUNT(b) FROM Building b WHERE b.residence.uuid = :residenceUuid AND b.isActive = true")
    long countActiveByResidenceUuid(@Param("residenceUuid") UUID residenceUuid);
}