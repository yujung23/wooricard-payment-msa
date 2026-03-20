package com.card.payment.pos.dto;

public record PaymentRequest(
        String primaryAccountNumber,    // DE02 - 카드번호
        String expirationDate,          // DE14 - 유효기간
        Long transactionAmount,         // DE04 - 거래금액
        String cardAcceptorId,          // DE42 - 가맹점ID
        int installmentMonths
) {}