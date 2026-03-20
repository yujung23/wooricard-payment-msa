package com.card.payment.van.dto;

// VAN이 POS로부터 카드 승인 요청을 받는 클래스
public record PosPaymentRequest(
        String primaryAccountNumber,  // DE02 - PAN (카드번호)
        String expirationDate,        // DE14 - Expiration Date (유효기간)
        Long transactionAmount,       // DE04 - Transaction Amount (거래금액)
        String cardAcceptorId,        // DE42 - Card Acceptor ID (가맹점ID)
        String terminalId,            // DE41 - 단말기 ID (선택, 없으면 null)
        int installmentMonths,        // 할부 개월 수
        String posOrderId             // POS 주문 ID
) { }
