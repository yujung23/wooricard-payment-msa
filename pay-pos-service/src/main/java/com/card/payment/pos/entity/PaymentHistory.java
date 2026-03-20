package com.card.payment.pos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String approvalId;
    private String cardNumber;
    private Long amount;
    private String status;
    private String cardCompany;
    private String merchantId;
    private LocalDateTime createdAt;

    @Builder
    public PaymentHistory(String approvalId, String cardNumber, Long amount, String status, String cardCompany, String merchantId) {
        this.approvalId = approvalId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.status = status;
        this.cardCompany = cardCompany;
        this.merchantId = merchantId;
        this.createdAt = LocalDateTime.now();
    }
}
