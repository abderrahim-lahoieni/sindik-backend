package com.sindikbackend.syndic.contract;
// src/main/java/com/sindikbackend/contract/controllers/ContractController.java

import com.sindikbackend.syndic.contract.dtos.requests.ContractCreateRequest;
import com.sindikbackend.syndic.contract.dtos.requests.ContractUpdateRequest;
import com.sindikbackend.syndic.contract.dtos.responses.ContractResponse;
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.contract.services.ContractCrudService;
import com.sindikbackend.syndic.contract.services.ContractPdfService;
import com.sindikbackend.auth.entities.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractCrudService contractCrudService;
    private final ContractPdfService contractPdfService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractCreateRequest request,
                                                            @AuthenticationPrincipal User currentUser) {
        log.info("REST request to create contract");
        return ResponseEntity.status(HttpStatus.CREATED).body(contractCrudService.createContract(
                request, currentUser.getUuid(), currentUser.getRole()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ContractResponse>> getAllContracts(
            @RequestParam(required = false) UUID residentUuid,
            @RequestParam(required = false) UUID unitUuid,
            @RequestParam(required = false) UUID buildingUuid,
            @RequestParam(required = false) ContractType type,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal User currentUser) {

        log.info("REST request to get contracts with filters");

        if (residentUuid != null || unitUuid != null || buildingUuid != null ||
                type != null || status != null || isActive != null) {
            return ResponseEntity.ok(contractCrudService.findByFilters(
                    residentUuid, unitUuid, buildingUuid, type, status, isActive,
                    currentUser.getUuid(), currentUser.getRole()));
        }

        return ResponseEntity.ok(contractCrudService.findAllContracts(currentUser.getUuid(), currentUser.getRole()));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ContractResponse> getContractByUuid(@PathVariable UUID uuid,
                                                               @AuthenticationPrincipal User currentUser) {
        log.info("REST request to get contract by uuid: {}", uuid);
        return ResponseEntity.ok(contractCrudService.findByUuid(uuid, currentUser.getUuid(), currentUser.getRole()));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable UUID uuid,
            @Valid @RequestBody ContractUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("REST request to update contract: {}", uuid);
        return ResponseEntity.ok(contractCrudService.updateContract(
                uuid, request, currentUser.getUuid(), currentUser.getRole()));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID uuid,
                                                @AuthenticationPrincipal User currentUser) {
        log.info("REST request to delete contract: {}", uuid);
        contractCrudService.deleteContract(uuid, currentUser.getUuid(), currentUser.getRole());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ContractResponse>> getContractsByBuilding(@PathVariable UUID buildingUuid,
                                                                           @AuthenticationPrincipal User currentUser) {
        log.info("REST request to get contracts by building: {}", buildingUuid);
        return ResponseEntity.ok(contractCrudService.findByBuildingUuid(
                buildingUuid, currentUser.getUuid(), currentUser.getRole()));
    }

    @GetMapping("/resident/{residentUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ContractResponse>> getContractsByResident(@PathVariable UUID residentUuid,
                                                                          @AuthenticationPrincipal User currentUser) {
        log.info("REST request to get contracts by resident: {}", residentUuid);
        return ResponseEntity.ok(contractCrudService.findByResidentUuid(
                residentUuid, currentUser.getUuid(), currentUser.getRole()));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<ContractResponse>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int daysAhead,
            @AuthenticationPrincipal User currentUser) {
        log.info("REST request to get expiring contracts in {} days", daysAhead);
        return ResponseEntity.ok(contractCrudService.findExpiringContracts(
                daysAhead, currentUser.getUuid(), currentUser.getRole()));
    }


    @GetMapping("/{uuid}/download-pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ByteArrayResource> downloadContractPdf(@PathVariable UUID uuid,
                                                                  @AuthenticationPrincipal User currentUser) {
        log.info("REST request to download PDF for contract: {}", uuid);

        try {
            contractCrudService.findByUuid(uuid, currentUser.getUuid(), currentUser.getRole());
            byte[] pdfBytes = contractPdfService.generateContractPdf(uuid);

            // Générer un nom de fichier unique
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = "Contrat_" + uuid.toString().substring(0, 8) + "_" + timestamp + ".pdf";

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error generating PDF for contract: {}", uuid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{uuid}/preview-pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<ByteArrayResource> previewContractPdf(@PathVariable UUID uuid,
                                                                 @AuthenticationPrincipal User currentUser) {
        log.info("REST request to preview PDF for contract: {}", uuid);

        try {
            contractCrudService.findByUuid(uuid, currentUser.getUuid(), currentUser.getRole());
            byte[] pdfBytes = contractPdfService.generateContractPdf(uuid);

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error generating PDF preview for contract: {}", uuid, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
