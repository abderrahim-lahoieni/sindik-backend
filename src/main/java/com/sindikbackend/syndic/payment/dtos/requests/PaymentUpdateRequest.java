package com.sindikbackend.syndic.payment.dtos.requests;
// src/main/java/com/sindikbackend/payment/dtos/requests/PaymentUpdateRequest.java

import com.sindikbackend.syndic.payment.enums.PaymentMethod;
import com.sindikbackend.syndic.payment.enums.PaymentStatus;
import com.sindikbackend.syndic.payment.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentUpdateRequest {

    private PaymentType type;

    private PaymentStatus status;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Amount paid cannot be negative")
    private BigDecimal amountPaid;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private PaymentMethod paymentMethod;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    @DecimalMin(value = "0.0", message = "Late fee cannot be negative")
    private BigDecimal lateFee;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discount;

    private String notes;

    private String receiptUrl;

    private Boolean isRecurring;
}