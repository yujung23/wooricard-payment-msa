package com.card.payment.pos.dto;

import java.time.LocalDateTime;

public record PaymentResponse (
        String approvalId,
        String status,
        String message,
        LocalDateTime approvedAt,
        String cardCompany) {
    public static PaymentResponse from (
            String approvalId, String status,
            String message, String cardCompany) {
        return new PaymentResponse(
                approvalId,
                status,
                message,
                LocalDateTime.now(),
                cardCompany);
    }
}
