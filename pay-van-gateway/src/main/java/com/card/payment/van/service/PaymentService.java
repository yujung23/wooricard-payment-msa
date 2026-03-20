package com.card.payment.van.service;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;

public interface PaymentService {

    // POS 승인 요청 처리 결과 반환
    PosPaymentResponse approvePayment(PosPaymentRequest request);

    // 거래ID(approvalId)로 결제 결과 조회
    PosPaymentResponse getPaymentResult(String approvalId);
}
