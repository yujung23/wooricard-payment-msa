package com.card.payment.van.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// VAN이 POS로부터 카드 승인 요청을 받는 클래스
public class PosPaymentRequest {
    private String cardNumber; // 카드번호
    private String expiryDate; // 카드 유효기간
    private Long amount; // 결제 금액
    private String posOrderId; // POS에서 관리하는 주문 ID
    private String merchantId;
}
