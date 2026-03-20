package com.card.payment.van.dto;

import java.time.LocalDateTime;

public record CardAuthorizationResponse(
        String transactionId,        // DE11 - STAN
        String approvalNumber,       // DE38 - Approval Code
        String responseCode,         // DE39 - Response Code
        String message,              // 응답 메시지
        Long amount,                 // DE04 - Transaction Amount
        LocalDateTime authorizationDate,
        boolean approved
) {}