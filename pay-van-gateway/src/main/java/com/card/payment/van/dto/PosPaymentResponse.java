package com.card.payment.van.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// VAN이 POS에게 카드 승인 결과를 보내주는 클래스
public class PosPaymentResponse {
    private String responseCode; // 응답 코드
    private String responseMessage; // 응답 메시지
    private String approvalNumber; // VAN에서 발생한 승인 번호
    private String transactionId; // 시스템에서 발급한 거래 고유 번호
    private String paymentDate; // 결제 승인 일시
    private String posOrderId; // POS에서 보내는 주문ID
}
