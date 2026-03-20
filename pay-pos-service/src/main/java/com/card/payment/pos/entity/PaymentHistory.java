package com.card.payment.pos.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String systemTraceAuditNumber;
    private String primaryAccountNumber;
    private Long transactionAmount;
    private String responseCode;
    private String cardCompany;
    private String cardAcceptorId;
    private LocalDateTime createdAt;

    @Builder
    public PaymentHistory(String systemTraceAuditNumber, String primaryAccountNumber,
                          Long transactionAmount, String responseCode,
                          String cardCompany, String cardAcceptorId) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
        this.primaryAccountNumber = primaryAccountNumber;
        this.transactionAmount = transactionAmount;
        this.responseCode = responseCode;
        this.cardCompany = cardCompany;
        this.cardAcceptorId = cardAcceptorId;
        this.createdAt = LocalDateTime.now();
    }
}