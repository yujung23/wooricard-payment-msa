package com.card.payment.pos.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public record PaymentRequest(
        String cardNumber,
        String expiryDate,
        Long amount,
        String merchantId,
        int installmentMonths
) {}