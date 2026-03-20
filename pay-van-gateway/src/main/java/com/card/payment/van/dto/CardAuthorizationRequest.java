package com.card.payment.van.dto;

import java.util.UUID;

public record CardAuthorizationRequest(
        String transactionId,      // DE11 - STAN (VAN이 생성)
        String cardNumber,         // DE02 - PAN
        Long amount,               // DE04 - Transaction Amount
        String merchantId,         // DE42 - Card Acceptor ID
        String terminalId,         // DE41 - 단말기 ID (POS에서 전달, 없으면 null)
        String pin                 // PIN (선택)
) {
    public static CardAuthorizationRequest from(PosPaymentRequest request) {
        return new CardAuthorizationRequest(
                UUID.randomUUID().toString(),        // transactionId - VAN이 생성
                request.primaryAccountNumber(),      // cardNumber
                request.transactionAmount(),         // amount
                request.cardAcceptorId(),            // merchantId
                request.terminalId(),                // terminalId - POS에서 전달
                null                                 // pin - 현재 POS 스펙에 없음
        );
    }
}