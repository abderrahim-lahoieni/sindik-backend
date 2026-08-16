package com.sindikbackend.syndic.payment.enums;
// src/main/java/com/sindikbackend/payment/entities/PaymentStatus.java
public enum PaymentStatus {
    PENDING,           // En attente
    PARTIAL,           // Partiellement payé
    PAID,              // Payé
    OVERDUE,           // En retard
    CANCELLED          // Annulé
}