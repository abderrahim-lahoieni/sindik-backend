package com.sindikbackend.syndic.syndicbuilding;// controllers/SyndicBuildingController.java

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sindikbackend.syndic.syndicbuilding.dto.requests.AssignSyndicRequest;
import com.sindikbackend.syndic.syndicbuilding.dto.responses.SyndicBuildingResponse;
import com.sindikbackend.syndic.syndicbuilding.services.SyndicBuildingService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/syndic-buildings")
@RequiredArgsConstructor
public class SyndicBuildingController {

    private final SyndicBuildingService syndicBuildingService;

    // ADMIN UNIQUEMENT : Assigner un syndic à des bâtiments
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SyndicBuildingResponse>> assignSyndic(
            @Valid @RequestBody AssignSyndicRequest request) {
        log.info("REST request to assign syndic to buildings");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(syndicBuildingService.assignSyndicToBuildings(request));
    }

    // Récupérer les bâtiments d'un syndic
    @GetMapping("/syndic/{syndicUuid}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#syndicUuid)")
    public ResponseEntity<List<SyndicBuildingResponse>> getBuildingsBySyndic(@PathVariable UUID syndicUuid) {
        log.info("REST request to get buildings for syndic: {}", syndicUuid);
        return ResponseEntity.ok(syndicBuildingService.getBuildingsBySyndic(syndicUuid));
    }

    // Récupérer les syndics d'un bâtiment
    @GetMapping("/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SyndicBuildingResponse>> getSyndicsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get syndics for building: {}", buildingUuid);
        return ResponseEntity.ok(syndicBuildingService.getSyndicsByBuilding(buildingUuid));
    }

    // Retirer un syndic d'un bâtiment
    @DeleteMapping("/syndic/{syndicUuid}/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeSyndicFromBuilding(
            @PathVariable UUID syndicUuid,
            @PathVariable UUID buildingUuid) {
        log.info("REST request to remove syndic {} from building {}", syndicUuid, buildingUuid);
        syndicBuildingService.removeSyndicFromBuilding(syndicUuid, buildingUuid);
        return ResponseEntity.noContent().build();
    }
}
