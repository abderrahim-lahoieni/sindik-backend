package com.sindikbackend.syndic.contract.dtos.requests;

// src/main/java/com/sindikbackend/contract/dtos/requests/ContractUpdateRequest.java
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.payment.enums.PaymentFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContractUpdateRequest {

    private UUID residentUuid;

    private UUID unitUuid;

    private ContractType type;

    private ContractStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be greater than 0")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    private Boolean depositPaid;

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
