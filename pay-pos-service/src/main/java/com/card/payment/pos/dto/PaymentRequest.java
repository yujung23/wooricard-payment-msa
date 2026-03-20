package com.card.payment.pos.dto;

public record PaymentRequest(
        String primaryAccountNumber,    // DE02 - 카드번호
        String expirationDate,          // DE14 - 유효기간
        Long transactionAmount,         // DE04 - 거래금액
        String cardAcceptorId,          // DE42 - 가맹점ID
        String terminalId,              // DE41 - 단말기 ID (선택, 없으면 null)
        int installmentMonths,          // 할부 개월수
        String posOrderId               // POS 주문 ID
) {}