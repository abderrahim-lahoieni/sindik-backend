package com.sindikbackend.syndic.contract.dtos.responses;

// src/main/java/com/sindikbackend/contract/dtos/responses/ContractResponse.java
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.payment.enums.PaymentFrequency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID uuid;
    private UUID residentUuid;
    private String residentName;
    private UUID unitUuid;
    private String unitNumber;
    private UUID buildingUuid;
    private String buildingName;
    private UUID residenceUuid;
    private String residenceName;
    private ContractType type;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private PaymentFrequency paymentFrequency;
    private Integer paymentDay;
    private String contractNumber;
    private String terms;
    private String notes;
    private Integer renewalNoticeDays;
    private Boolean autoRenewable;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}