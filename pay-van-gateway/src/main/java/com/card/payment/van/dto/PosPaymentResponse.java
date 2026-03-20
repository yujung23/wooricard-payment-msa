package com.card.payment.van.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
// VAN이 POS에게 카드 승인 결과를 보내주는 클래스
public class PosPaymentResponse {
    private String approvalId;    // pay-pos-service approvalId()와 매칭
    private String status;        // pay-pos-service status()와 매칭
    private String message;       // pay-pos-service message()와 매칭
    private LocalDateTime approvedAt; // pay-pos-service approvedAt()과 매칭
    private String cardCompany;   // pay-pos-service cardCompany()와 매칭
    private String posOrderId;    // 관리용 ID
}