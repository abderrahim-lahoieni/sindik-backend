package com.sindikbackend.syndic.payment.services;

// src/main/java/com/sindikbackend/payment/services/PaymentCrudService.java
import com.sindikbackend.admin.resident.entities.Resident;
import com.sindikbackend.admin.resident.repositories.ResidentRepository;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;

import com.sindikbackend.syndic.contract.entities.Contract;
import com.sindikbackend.syndic.payment.dtos.requests.PaymentCreateRequest;
import com.sindikbackend.syndic.payment.dtos.requests.PaymentUpdateRequest;
import com.sindikbackend.syndic.payment.dtos.responses.PaymentResponse;
import com.sindikbackend.syndic.payment.entities.Payment;
import com.sindikbackend.syndic.contract.repositories.ContractRepository;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import com.sindikbackend.syndic.payment.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCrudService {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final ResidentRepository residentRepository;

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .uuid(payment.getUuid())
                .contractUuid(payment.getContract().getUuid())
                .contractNumber(payment.getContract().getContractNumber())
                .residentUuid(payment.getResident().getUuid())
                .residentName(payment.getResident().getFullName())
                .unitUuid(payment.getContract().getUnit().getUuid())
                .unitNumber(payment.getContract().getUnit().getUnitNumber())
                .buildingUuid(payment.getContract().getUnit().getBuilding().getUuid())
                .buildingName(payment.getContract().getUnit().getBuilding().getName())
                .type(payment.getType())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .amountPaid(payment.getAmountPaid())
                .remainingAmount(payment.getRemainingAmount())
                .dueDate(payment.getDueDate())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .paymentReference(payment.getPaymentReference())
                .periodStart(payment.getPeriodStart())
                .periodEnd(payment.getPeriodEnd())
                .lateFee(payment.getLateFee())
                .discount(payment.getDiscount())
                .notes(payment.getNotes())
                .receiptUrl(payment.getReceiptUrl())
                .isRecurring(payment.getIsRecurring())
                .isLate(payment.isLate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        log.info("Creating new payment for contract: {}", request.getContractUuid());

        // Vérifier que le contrat existe
        Contract contract = contractRepository.findByUuid(request.getContractUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found with uuid: " + request.getContractUuid()));

        // Vérifier que le résident existe
        Resident resident = residentRepository.findByUuid(request.getResidentUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resident not found with uuid: " + request.getResidentUuid()));

        // Validation des montants
        if (request.getAmountPaid() != null && request.getAmountPaid().compareTo(request.getAmount()) > 0) {
            throw new BadRequestException("Amount paid cannot be greater than total amount");
        }

        Payment payment = Payment.builder()
                .contract(contract)
                .resident(resident)
                .type(request.getType())
                .status(request.getStatus())
                .amount(request.getAmount())
                .amountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO)
                .dueDate(request.getDueDate())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .lateFee(request.getLateFee() != null ? request.getLateFee() : BigDecimal.ZERO)
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .receiptUrl(request.getReceiptUrl())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .build();

        // Mettre à jour le statut automatiquement
        updatePaymentStatus(payment);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with reference: {}", savedPayment.getPaymentReference());

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAllPayments() {
        log.info("Finding all payments");
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse findByUuid(UUID uuid) {
        log.info("Finding payment by uuid: {}", uuid);
        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with uuid: " + uuid));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse updatePayment(UUID uuid, PaymentUpdateRequest request) {
        log.info("Updating payment: {}", uuid);

        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with uuid: " + uuid));

        if (request.getType() != null) payment.setType(request.getType());
        if (request.getStatus() != null) payment.setStatus(request.getStatus());
        if (request.getAmount() != null) payment.setAmount(request.getAmount());
        if (request.getAmountPaid() != null) payment.setAmountPaid(request.getAmountPaid());
        if (request.getDueDate() != null) payment.setDueDate(request.getDueDate());
        if (request.getPaymentDate() != null) payment.setPaymentDate(request.getPaymentDate());
        if (request.getPaymentMethod() != null) payment.setPaymentMethod(request.getPaymentMethod());
        if (request.getPeriodStart() != null) payment.setPeriodStart(request.getPeriodStart());
        if (request.getPeriodEnd() != null) payment.setPeriodEnd(request.getPeriodEnd());
        if (request.getLateFee() != null) payment.setLateFee(request.getLateFee());
        if (request.getDiscount() != null) payment.setDiscount(request.getDiscount());
        if (request.getNotes() != null) payment.setNotes(request.getNotes());
        if (request.getReceiptUrl() != null) payment.setReceiptUrl(request.getReceiptUrl());
        if (request.getIsRecurring() != null) payment.setIsRecurring(request.getIsRecurring());

        // Mettre à jour le statut automatiquement
        updatePaymentStatus(payment);

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated successfully: {}", uuid);

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public void deletePayment(UUID uuid) {
        log.info("Deleting payment: {}", uuid);
        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with uuid: " + uuid));
        paymentRepository.delete(payment);
        log.info("Payment deleted successfully: {}", uuid);
    }

    // Méthodes spécifiques

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByContractUuid(UUID contractUuid) {
        log.info("Finding payments by contract: {}", contractUuid);
        return paymentRepository.findByContractUuid(contractUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByResidentUuid(UUID residentUuid) {
        log.info("Finding payments by resident: {}", residentUuid);
        return paymentRepository.findByResidentUuid(residentUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByBuildingUuid(UUID buildingUuid) {
        log.info("Finding payments by building: {}", buildingUuid);
        return paymentRepository.findByBuildingUuid(buildingUuid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findOverduePayments() {
        log.info("Finding overdue payments");
        return paymentRepository.findOverdue(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findUpcomingPayments(int daysAhead) {
        log.info("Finding upcoming payments in {} days", daysAhead);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        return paymentRepository.findUpcomingBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByFilters(UUID contractUuid, UUID residentUuid, UUID buildingUuid,
                                               PaymentType type, PaymentStatus status,
                                               LocalDate startDate, LocalDate endDate) {
        log.info("Finding payments with filters");
        return paymentRepository.findByFilters(contractUuid, residentUuid, buildingUuid,
                        type, status, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse recordPayment(UUID uuid, BigDecimal amount, PaymentUpdateRequest request) {
        log.info("Recording payment for: {}", uuid);

        Payment payment = paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with uuid: " + uuid));

        BigDecimal newAmountPaid = payment.getAmountPaid().add(amount);

        if (newAmountPaid.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Total amount paid cannot exceed payment amount");
        }

        payment.setAmountPaid(newAmountPaid);
        payment.setPaymentDate(LocalDate.now());

        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }
        if (request.getReceiptUrl() != null) {
            payment.setReceiptUrl(request.getReceiptUrl());
        }

        // Mettre à jour le statut
        updatePaymentStatus(payment);

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment recorded successfully: {}", uuid);

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public void markAsOverdue() {
        log.info("Marking overdue payments");
        List<Payment> overduePayments = paymentRepository.findOverdue(LocalDate.now());

        for (Payment payment : overduePayments) {
            if (payment.getStatus() != PaymentStatus.PAID && payment.getStatus() != PaymentStatus.CANCELLED) {
                payment.setStatus(PaymentStatus.OVERDUE);
                paymentRepository.save(payment);
            }
        }

        log.info("Marked {} payments as overdue", overduePayments.size());
    }

    // Méthode utilitaire pour mettre à jour le statut du paiement
    private void updatePaymentStatus(Payment payment) {
        BigDecimal remaining = payment.getRemainingAmount();

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            payment.setStatus(PaymentStatus.PAID);
        } else if (payment.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            payment.setStatus(PaymentStatus.PARTIAL);
        } else if (payment.isLate()) {
            payment.setStatus(PaymentStatus.OVERDUE);
        }
    }

    // Statistiques financières
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByBuilding(UUID buildingUuid) {
        BigDecimal total = paymentRepository.sumPaidByBuildingUuid(buildingUuid);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByBuilding(UUID buildingUuid) {
        BigDecimal total = paymentRepository.sumPendingByBuildingUuid(buildingUuid);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidInPeriod(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = paymentRepository.sumPaidInPeriod(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
}
