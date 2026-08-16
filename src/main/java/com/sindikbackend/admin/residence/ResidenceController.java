package com.sindikbackend.admin.residence;

import com.sindikbackend.admin.residence.dtos.requests.ResidenceCreateRequest;
import com.sindikbackend.admin.residence.dtos.requests.ResidenceUpdateRequest;
import com.sindikbackend.admin.residence.dtos.responses.ResidenceResponse;
import com.sindikbackend.admin.residence.services.ResidenceCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/residences")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ResidenceController {

    private final ResidenceCrudService residenceCrudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidenceResponse>> getAllResidences(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("REST request to get residences | type: {} | active: {}", type, active);
        return ResponseEntity.ok(residenceCrudService.findResidences(type, active));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ResidenceResponse> getResidenceByUuid(@PathVariable UUID uuid) {
        log.info("REST request to get residence by uuid: {}", uuid);
        return ResponseEntity.ok(residenceCrudService.findResidenceByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidenceResponse> createResidence(@Valid @RequestBody ResidenceCreateRequest request) {
        log.info("REST request to create residence: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(residenceCrudService.createResidence(request));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidenceResponse> updateResidence(
            @PathVariable UUID uuid,
            @Valid @RequestBody ResidenceUpdateRequest request
    ) {
        log.info("REST request to update residence: {}", uuid);
        return ResponseEntity.ok(residenceCrudService.updateResidence(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResidence(@PathVariable UUID uuid) {
        log.info("REST request to delete residence: {}", uuid);
        residenceCrudService.deleteResidence(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResidenceResponse> toggleResidenceStatus(@PathVariable UUID uuid) {
        log.info("REST request to toggle status for residence: {}", uuid);
        return ResponseEntity.ok(residenceCrudService.toggleResidenceStatus(uuid));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidenceResponse>> searchResidences(@RequestParam String keyword) {
        log.info("REST request to search residences with keyword: {}", keyword);
        return ResponseEntity.ok(residenceCrudService.searchResidences(keyword));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidenceResponse>> getActiveResidences() {
        log.info("REST request to get active residences");
        return ResponseEntity.ok(residenceCrudService.findActiveResidences());
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ResidenceResponse>> getResidencesByType(@PathVariable String type) {
        log.info("REST request to get residences by type: {}", type);
        return ResponseEntity.ok(residenceCrudService.findResidencesByType(type));
    }
}