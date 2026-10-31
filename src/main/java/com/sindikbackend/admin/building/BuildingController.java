package com.sindikbackend.admin.building;

import com.sindikbackend.admin.building.dtos.requests.BuildingCreateRequest;
import com.sindikbackend.admin.building.dtos.requests.BuildingUpdateRequest;
import com.sindikbackend.admin.building.dtos.responses.BuildingResponse;
import com.sindikbackend.admin.building.services.BuildingCrudService;
import com.sindikbackend.auth.entities.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingCrudService buildingCrudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<BuildingResponse>> getAllBuildings(
            @RequestParam(required = false) UUID residenceUuid,
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal UserDetails userDetails // AJOUTER
    ) {
        log.info("REST request to get buildings | residence: {} | active: {}", residenceUuid, active);

        // Récupérer l'utilisateur connecté
        User currentUser = (User) userDetails;

        return ResponseEntity.ok(
                buildingCrudService.findBuildings(residenceUuid, active, currentUser.getUuid(), currentUser.getRole())
        );
    }

    @GetMapping("/residence/{residenceUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<BuildingResponse>> getBuildingsByResidence(@PathVariable UUID residenceUuid) {
        log.info("REST request to get buildings by residence: {}", residenceUuid);
        return ResponseEntity.ok(buildingCrudService.findByResidenceUuid(residenceUuid));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingResponse> createBuilding(@Valid @RequestBody BuildingCreateRequest request) {
        log.info("REST request to create building: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingCrudService.createBuilding(request));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @PathVariable UUID uuid,
            @Valid @RequestBody BuildingUpdateRequest request
    ) {
        log.info("REST request to update building: {}", uuid);
        return ResponseEntity.ok(buildingCrudService.updateBuilding(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBuilding(@PathVariable UUID uuid) {
        log.info("REST request to delete building: {}", uuid);
        buildingCrudService.deleteBuilding(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingResponse> toggleBuildingStatus(@PathVariable UUID uuid) {
        log.info("REST request to toggle status for building: {}", uuid);
        return ResponseEntity.ok(buildingCrudService.toggleBuildingStatus(uuid));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<BuildingResponse>> searchBuildings(@RequestParam String keyword) {
        log.info("REST request to search buildings with keyword: {}", keyword);
        return ResponseEntity.ok(buildingCrudService.searchBuildings(keyword));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<BuildingResponse>> getActiveBuildings() {
        log.info("REST request to get active buildings");
        return ResponseEntity.ok(buildingCrudService.findActiveBuildings());
    }

    @GetMapping("/residence/{residenceUuid}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countBuildingsByResidence(@PathVariable UUID residenceUuid) {
        log.info("REST request to count buildings for residence: {}", residenceUuid);
        return ResponseEntity.ok(buildingCrudService.countBuildingsByResidence(residenceUuid));
    }
}
