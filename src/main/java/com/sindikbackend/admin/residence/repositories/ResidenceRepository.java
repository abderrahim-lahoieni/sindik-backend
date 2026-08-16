package com.sindikbackend.admin.residence.repositories;

import com.sindikbackend.admin.residence.entities.Residence;
import com.sindikbackend.admin.residence.entities.ResidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidenceRepository extends JpaRepository<Residence, Long> {

    Optional<Residence> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("SELECT r FROM Residence r WHERE r.isActive = true ORDER BY r.createdAt DESC")
    List<Residence> findAllActive();

    @Query("SELECT r FROM Residence r WHERE r.type = :type AND r.isActive = true")
    List<Residence> findByTypeActive(@Param("type") ResidenceType type);

    @Query("SELECT r FROM Residence r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Residence> searchByNameOrCity(@Param("keyword") String keyword);

    @Query("""
        SELECT r FROM Residence r 
        WHERE (:type IS NULL OR r.type = :type) 
        AND (:active IS NULL OR r.isActive = :active)
        ORDER BY r.createdAt DESC
    """)
    List<Residence> findByFilters(
            @Param("type") ResidenceType type,
            @Param("active") Boolean active
    );

    @Query("SELECT COUNT(r) FROM Residence r WHERE r.type = :type")
    long countByType(@Param("type") ResidenceType type);
}