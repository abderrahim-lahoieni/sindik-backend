package com.sindikbackend.syndic.contract.dtos.requests;
// src/main/java/com/sindikbackend/contract/dtos/requests/ContractCreateRequest.java

import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.payment.enums.PaymentFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContractCreateRequest {

    @NotNull(message = "Resident UUID is required")
    private UUID residentUuid;

    @NotNull(message = "Unit UUID is required")
    private UUID unitUuid;

    @NotNull(message = "Contract type is required")
    private ContractType type;

    @NotNull(message = "Contract status is required")
    private ContractStatus status;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be greater than 0")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    private Boolean depositPaid;

    @NotNull(message = "Payment frequency is required")
    private PaymentFrequency paymentFrequency;

    @Min(value = 1, message = "Payment day must be between 1 and 31")
    @Max(value = 31, message = "Payment day must be between 1 and 31")
    private Integer paymentDay;

    private String terms;

    private String notes;

    @Min(value = 0, message = "Renewal notice days cannot be negative")
    private Integer renewalNoticeDays;

    private Boolean autoRenewable;

    private Boolean isActive;
}