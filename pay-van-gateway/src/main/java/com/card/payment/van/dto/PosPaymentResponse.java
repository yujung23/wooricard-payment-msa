package com.card.payment.van.dto;

import java.time.LocalDateTime;

// VAN이 POS에게 카드 승인 결과를 보내주는 클래스
public record PosPaymentResponse(
        String systemTraceAuditNumber,  // DE11 - STAN (거래 고유번호)
        String responseCode,            // DE39 - Response Code
        String responseMessage,         // 응답 메시지
        LocalDateTime approvedAt,       // 승인 일시
        String cardCompany,             // 카드사명
        String posOrderId               // POS 주문 ID (관리용)
) {
    public static PosPaymentResponse from(CardAuthorizationResponse response, String posOrderId, String cardCompany) {
        return new PosPaymentResponse(
                response.transactionId(),       // systemTraceAuditNumber (STAN)
                response.responseCode(),        // responseCode
                response.message(),             // responseMessage
                response.authorizationDate(),   // approvedAt
                cardCompany,                    // cardCompany (VAN이 BIN으로 판단)
                posOrderId                      // posOrderId
        );
    }
}