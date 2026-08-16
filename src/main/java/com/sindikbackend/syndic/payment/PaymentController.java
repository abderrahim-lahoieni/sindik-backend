package com.sindikbackend.syndic.payment;
// src/main/java/com/sindikbackend/payment/controllers/PaymentController.java
import com.sindikbackend.syndic.payment.dtos.requests.PaymentCreateRequest;
import com.sindikbackend.syndic.payment.dtos.requests.PaymentUpdateRequest;
import com.sindikbackend.syndic.payment.dtos.responses.PaymentResponse;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import com.sindikbackend.syndic.payment.services.PaymentCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private final PaymentCrudService paymentCrudService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("REST request to create payment");
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCrudService.createPayment(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) UUID contractUuid,
            @RequestParam(required = false) UUID residentUuid,
            @RequestParam(required = false) UUID buildingUuid,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get payments with filters");

        if (contractUuid != null || residentUuid != null || buildingUuid != null ||
                type != null || status != null || startDate != null || endDate != null) {
            return ResponseEntity.ok(paymentCrudService.findByFilters(
                    contractUuid, residentUuid, buildingUuid, type, status, startDate, endDate));
        }

        return ResponseEntity.ok(paymentCrudService.findAllPayments());
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<PaymentResponse> getPaymentByUuid(@PathVariable UUID uuid) {
        log.info("REST request to get payment by uuid: {}", uuid);
        return ResponseEntity.ok(paymentCrudService.findByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable UUID uuid,
            @Valid @RequestBody PaymentUpdateRequest request) {
        log.info("REST request to update payment: {}", uuid);
        return ResponseEntity.ok(paymentCrudService.updatePayment(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID uuid) {
        log.info("REST request to delete payment: {}", uuid);
        paymentCrudService.deletePayment(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/record")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<PaymentResponse> recordPayment(
            @PathVariable UUID uuid,
            @RequestParam BigDecimal amount,
            @Valid @RequestBody PaymentUpdateRequest request) {
        log.info("REST request to record payment: {}", uuid);
        return ResponseEntity.ok(paymentCrudService.recordPayment(uuid, amount, request));
    }

    @GetMapping("/contract/{contractUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByContract(@PathVariable UUID contractUuid) {
        log.info("REST request to get payments by contract: {}", contractUuid);
        return ResponseEntity.ok(paymentCrudService.findByContractUuid(contractUuid));
    }

    @GetMapping("/resident/{residentUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByResident(@PathVariable UUID residentUuid) {
        log.info("REST request to get payments by resident: {}", residentUuid);
        return ResponseEntity.ok(paymentCrudService.findByResidentUuid(residentUuid));
    }

    @GetMapping("/building/{buildingUuid}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get payments by building: {}", buildingUuid);
        return ResponseEntity.ok(paymentCrudService.findByBuildingUuid(buildingUuid));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getOverduePayments() {
        log.info("REST request to get overdue payments");
        return ResponseEntity.ok(paymentCrudService.findOverduePayments());
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<List<PaymentResponse>> getUpcomingPayments(
            @RequestParam(defaultValue = "30") int daysAhead) {
        log.info("REST request to get upcoming payments in {} days", daysAhead);
        return ResponseEntity.ok(paymentCrudService.findUpcomingPayments(daysAhead));
    }

    @GetMapping("/building/{buildingUuid}/total-paid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<BigDecimal> getTotalPaidByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get total paid for building: {}", buildingUuid);
        return ResponseEntity.ok(paymentCrudService.getTotalPaidByBuilding(buildingUuid));
    }

    @GetMapping("/building/{buildingUuid}/total-pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDIC')")
    public ResponseEntity<BigDecimal> getTotalPendingByBuilding(@PathVariable UUID buildingUuid) {
        log.info("REST request to get total pending for building: {}", buildingUuid);
        return ResponseEntity.ok(paymentCrudService.getTotalPendingByBuilding(buildingUuid));
    }
}