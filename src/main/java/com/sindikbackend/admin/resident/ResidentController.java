package com.sindikbackend.admin.resident;

import com.sindikbackend.admin.resident.dtos.requests.ResidentCreateRequest;
import com.sindikbackend.admin.resident.dtos.requests.ResidentUpdateRequest;
import com.sindikbackend.admin.resident.dtos.responses.ResidentResponse;
import com.sindikbackend.admin.resident.services.ResidentCrudService;
import com.sindikbackend.admin.unit.services.UnitCrudService;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.exceptions.BadRequestException;
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
@RequestMapping("/residents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ResidentController {

    private final ResidentCrudService residentCrudService;
    private final UnitCrudService unitCrudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getAllResidents(
            @RequestParam(required = false) UUID unitUuid,
            @RequestParam(required = false) UUID buildingUuid,
            @RequestParam(required = false) UUID residenceUuid,
            @RequestParam(required = false) Boolean isOwner,
            @RequestParam(required = false) Boolean isPrimaryResident,
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal UserDetails userDetails // AJOUTER
    ) {
        log.info("REST request to get residents | unit: {} | building: {} | residence: {} | owner: {} | primary: {} | active: {}",
                unitUuid, buildingUuid, residenceUuid, isOwner, isPrimaryResident, active);

        User currentUser = (User) userDetails;

        return ResponseEntity.ok(
                residentCrudService.findResidents(unitUuid, buildingUuid, residenceUuid,
                        isOwner, isPrimaryResident, active, currentUser.getUuid(), currentUser.getRole())
        );
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ResidentResponse> getResidentByUuid(@PathVariable UUID uuid) {
        log.info("REST request to get resident by uuid: {}", uuid);
        return ResponseEntity.ok(residentCrudService.findResidentByUuid(uuid));
    }

    @GetMapping("/unit/{unitUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getResidentsByUnit(@PathVariable UUID unitUuid) {
        log.info("REST request to get residents by unit: {}", unitUuid);
        return ResponseEntity.ok(residentCrudService.findByUnitUuid(unitUuid));
    }

    @GetMapping("/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getResidentsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get residents by building: {}", buildingUuid);
        return ResponseEntity.ok(residentCrudService.findByBuildingUuid(buildingUuid));
    }

    @GetMapping("/residence/{residenceUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getResidentsByResidence(@PathVariable UUID residenceUuid) {
        log.info("REST request to get residents by residence: {}", residenceUuid);
        return ResponseEntity.ok(residentCrudService.findByResidenceUuid(residenceUuid));
    }

    @GetMapping("/user/{userUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC') or @userSecurity.isCurrentUser(#userUuid)")
    public ResponseEntity<ResidentResponse> getResidentByUser(@PathVariable UUID userUuid) {
        log.info("REST request to get resident by user: {}", userUuid);
        return ResponseEntity.ok(residentCrudService.findByUserUuid(userUuid));
    }

    // AJOUTER une sécurité pour la création
    @PostMapping
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<ResidentResponse> createResident(
            @Valid @RequestBody ResidentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("REST request to create resident: {}", request.getFullName());

        User currentUser = (User) userDetails;

        // Si SYNDIC, vérifier qu'il peut gérer l'unité
        if (currentUser.getRole() == Role.SYNDIC) {
            if (!unitCrudService.canManageUnit(currentUser.getUuid(), request.getUnitUuid())) {
                throw new BadRequestException("You cannot manage this unit");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(residentCrudService.createResident(request));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<ResidentResponse> updateResident(
            @PathVariable UUID uuid,
            @Valid @RequestBody ResidentUpdateRequest request
    ) {
        log.info("REST request to update resident: {}", uuid);
        return ResponseEntity.ok(residentCrudService.updateResident(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<Void> deleteResident(@PathVariable UUID uuid) {
        log.info("REST request to delete resident: {}", uuid);
        residentCrudService.deleteResident(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidentResponse> toggleResidentStatus(@PathVariable UUID uuid) {
        log.info("REST request to toggle status for resident: {}", uuid);
        return ResponseEntity.ok(residentCrudService.toggleResidentStatus(uuid));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> searchResidents(@RequestParam String keyword) {
        log.info("REST request to search residents with keyword: {}", keyword);
        return ResponseEntity.ok(residentCrudService.searchResidents(keyword));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getActiveResidents() {
        log.info("REST request to get active residents");
        return ResponseEntity.ok(residentCrudService.findActiveResidents());
    }

    @GetMapping("/owners")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getOwners() {
        log.info("REST request to get owner residents");
        return ResponseEntity.ok(residentCrudService.findOwners());
    }

    @GetMapping("/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidentResponse>> getPrimaryResidents() {
        log.info("REST request to get primary residents");
        return ResponseEntity.ok(residentCrudService.findPrimaryResidents());
    }

    @GetMapping("/unit/{unitUuid}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countResidentsByUnit(@PathVariable UUID unitUuid) {
        log.info("REST request to count residents for unit: {}", unitUuid);
        return ResponseEntity.ok(residentCrudService.countResidentsByUnit(unitUuid));
    }

    @GetMapping("/building/{buildingUuid}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countResidentsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to count residents for building: {}", buildingUuid);
        return ResponseEntity.ok(residentCrudService.countResidentsByBuilding(buildingUuid));
    }

    @GetMapping("/residence/{residenceUuid}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countResidentsByResidence(@PathVariable UUID residenceUuid) {
        log.info("REST request to count residents for residence: {}", residenceUuid);
        return ResponseEntity.ok(residentCrudService.countResidentsByResidence(residenceUuid));
    }

    @GetMapping("/owners/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Long> countOwners() {
        log.info("REST request to count owner residents");
        return ResponseEntity.ok(residentCrudService.countOwners());
    }
}