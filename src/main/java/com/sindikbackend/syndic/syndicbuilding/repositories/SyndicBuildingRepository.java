package com.sindikbackend.syndic.syndicbuilding.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.sindikbackend.syndic.syndicbuilding.entities.SyndicBuilding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyndicBuildingRepository extends JpaRepository<SyndicBuilding, Long> {

    Optional<SyndicBuilding> findByUuid(UUID uuid);

    // Récupérer tous les bâtiments d'un syndic
    @Query("SELECT sb FROM SyndicBuilding sb WHERE sb.syndic.uuid = :syndicUuid")
    List<SyndicBuilding> findBySyndicUuid(@Param("syndicUuid") UUID syndicUuid);

    // Récupérer tous les syndics d'un bâtiment
    @Query("SELECT sb FROM SyndicBuilding sb WHERE sb.building.uuid = :buildingUuid")
    List<SyndicBuilding> findByBuildingUuid(@Param("buildingUuid") UUID buildingUuid);

    // Vérifier si un syndic gère un bâtiment spécifique
    @Query("SELECT CASE WHEN COUNT(sb) > 0 THEN true ELSE false END FROM SyndicBuilding sb " +
            "WHERE sb.syndic.uuid = :syndicUuid AND sb.building.uuid = :buildingUuid")
    boolean existsBySyndicAndBuilding(@Param("syndicUuid") UUID syndicUuid,
                                      @Param("buildingUuid") UUID buildingUuid);

    // Supprimer l'assignation d'un syndic à un bâtiment
    void deleteBySyndicUuidAndBuildingUuid(UUID syndicUuid, UUID buildingUuid);
}