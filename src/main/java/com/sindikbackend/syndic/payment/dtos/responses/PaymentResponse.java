package com.sindikbackend.syndic.payment.dtos.responses;
// src/main/java/com/sindikbackend/payment/dtos/responses/PaymentResponse.java
import com.sindikbackend.syndic.payment.enums.PaymentMethod;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID uuid;
    private UUID contractUuid;
    private String contractNumber;
    private UUID residentUuid;
    private String residentName;
    private UUID unitUuid;
    private String unitNumber;
    private UUID buildingUuid;
    private String buildingName;
    private PaymentType type;
    private PaymentStatus status;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal lateFee;
    private BigDecimal discount;
    private String notes;
    private String receiptUrl;
    private Boolean isRecurring;
    private Boolean isLate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}