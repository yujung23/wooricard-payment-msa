package com.card.payment.pos.service;

import com.card.payment.pos.client.VanClient;
import com.card.payment.pos.dto.PaymentRequest;
import com.card.payment.pos.dto.PaymentResponse;
import com.card.payment.pos.entity.PaymentHistory;
import com.card.payment.pos.entity.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VanClient vanClient;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public PaymentResponse requestPayment(PaymentRequest request) {
        // 1. VaN 서비스에 승인 요청
        PaymentResponse response = vanClient.requestApproval(request);

        // 2. 결제 이력 저장
        paymentHistoryRepository.save(
                PaymentHistory.builder()
                        .approvalId(response.approvalId())
                        .cardNumber(maskCardNumber(request.cardNumber()))
                        .amount(request.amount())
                        .status(response.status())
                        .cardCompany(response.cardCompany())
                        .merchantId(request.merchantId())
                        .build()
        );

        // 3. 결과 반환
        return response;
    }

    public PaymentResponse getPaymentResult(String approvalId) {
        PaymentHistory history = paymentHistoryRepository.findByApprovalId(approvalId)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다: " + approvalId));

        return PaymentResponse.from(
                history.getApprovalId(),
                history.getStatus(),
                "조회 완료",
                history.getCardCompany()
        );
    }

    private String maskCardNumber(String cardNumber) {
        return cardNumber.substring(0, 4) + "-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

}
