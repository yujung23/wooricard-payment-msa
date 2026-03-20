package com.card.payment.pos.dto;

import java.time.LocalDateTime;

public record PaymentResponse(
        String systemTraceAuditNumber,  // DE11 - 거래 고유번호
        String responseCode,            // DE39 - 응답코드
        String responseMessage,
        LocalDateTime approvedAt,
        String cardCompany
) {
    public static PaymentResponse from(
            String stan, String responseCode,
            String responseMessage, String cardCompany) {
        return new PaymentResponse(
                stan, responseCode, responseMessage,
                LocalDateTime.now(), cardCompany);
    }
}