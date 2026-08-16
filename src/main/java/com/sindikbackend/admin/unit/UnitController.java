package com.sindikbackend.admin.unit;

import com.sindikbackend.admin.unit.dtos.requests.UnitCreateRequest;
import com.sindikbackend.admin.unit.dtos.requests.UnitUpdateRequest;
import com.sindikbackend.admin.unit.dtos.responses.UnitResponse;
import com.sindikbackend.admin.unit.services.UnitCrudService;
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
@RequestMapping("/units")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UnitController {

    private final UnitCrudService unitCrudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getAllUnits(
            @RequestParam(required = false) UUID buildingUuid,
            @RequestParam(required = false) UUID residenceUuid,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean occupied,
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal UserDetails userDetails // AJOUTER
    ) {
        log.info("REST request to get units | building: {} | residence: {} | type: {} | occupied: {} | active: {}",
                buildingUuid, residenceUuid, type, occupied, active);

        User currentUser = (User) userDetails;

        return ResponseEntity.ok(
                unitCrudService.findUnits(buildingUuid, residenceUuid, type, occupied, active,
                        currentUser.getUuid(), currentUser.getRole())
        );
    }



    @GetMapping("/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getUnitsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get units by building: {}", buildingUuid);
        return ResponseEntity.ok(unitCrudService.findByBuildingUuid(buildingUuid));
    }

    @GetMapping("/residence/{residenceUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getUnitsByResidence(@PathVariable UUID residenceUuid) {
        log.info("REST request to get units by residence: {}", residenceUuid);
        return ResponseEntity.ok(unitCrudService.findByResidenceUuid(residenceUuid));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody UnitCreateRequest request) {
        log.info("REST request to create unit: {}", request.getUnitNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(unitCrudService.createUnit(request));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> updateUnit(
            @PathVariable UUID uuid,
            @Valid @RequestBody UnitUpdateRequest request
    ) {
        log.info("REST request to update unit: {}", uuid);
        return ResponseEntity.ok(unitCrudService.updateUnit(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable UUID uuid) {
        log.info("REST request to delete unit: {}", uuid);
        unitCrudService.deleteUnit(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> toggleUnitStatus(@PathVariable UUID uuid) {
        log.info("REST request to toggle status for unit: {}", uuid);
        return ResponseEntity.ok(unitCrudService.toggleUnitStatus(uuid));
    }

    @PatchMapping("/{uuid}/toggle-occupancy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<UnitResponse> toggleUnitOccupancy(@PathVariable UUID uuid) {
        log.info("REST request to toggle occupancy for unit: {}", uuid);
        return ResponseEntity.ok(unitCrudService.toggleUnitOccupancy(uuid));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> searchUnits(@RequestParam String keyword) {
        log.info("REST request to search units with keyword: {}", keyword);
        return ResponseEntity.ok(unitCrudService.searchUnits(keyword));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getActiveUnits() {
        log.info("REST request to get active units");
        return ResponseEntity.ok(unitCrudService.findActiveUnits());
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getAvailableUnits() {
        log.info("REST request to get available units");
        return ResponseEntity.ok(unitCrudService.findAvailableUnits());
    }

    @GetMapping("/occupied")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getOccupiedUnits() {
        log.info("REST request to get occupied units");
        return ResponseEntity.ok(unitCrudService.findOccupiedUnits());
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<UnitResponse>> getUnitsByType(@PathVariable String type) {
        log.info("REST request to get units by type: {}", type);
        return ResponseEntity.ok(unitCrudService.findUnitsByType(type));
    }

    @GetMapping("/building/{buildingUuid}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countUnitsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to count units for building: {}", buildingUuid);
        return ResponseEntity.ok(unitCrudService.countUnitsByBuilding(buildingUuid));
    }

    @GetMapping("/building/{buildingUuid}/count-available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countAvailableUnitsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to count available units for building: {}", buildingUuid);
        return ResponseEntity.ok(unitCrudService.countAvailableUnitsByBuilding(buildingUuid));
    }
}