package com.card.payment.van.service;

import com.card.payment.van.client.CardClient;
import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import com.card.payment.van.entity.PaymentHistory;
import com.card.payment.van.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final CardClient cardClient;

    @Override
    @Transactional
    public PosPaymentResponse approvePayment(PosPaymentRequest request) {
        log.info("[VAN Gateway] 승인 요청 수신 - POS 주문번호: {}, 결제금액: {}",
                request.getPosOrderId(), request.getAmount());

        // 1. 비즈니스 검증: 결제 금액이 1억 원을 초과하면 즉시 거절
        if (request.getAmount() > 100_000_000) {
            log.warn("[VAN Gateway] 승인 거절 - 한도 초과: {}", request.getAmount());
            return buildFailResponse(request, "결제 한도가 초과되었습니다.");
        }

        // 2. 외부 통신: CardClient를 사용하여 실제 카드사 승인 요청 수행
        PosPaymentResponse cardResponse = cardClient.requestCardApproval(request);

        // 3. DB 저장: 카드사 응답 결과를 바탕으로 결제 이력 저장
        PaymentHistory history = PaymentHistory.builder()
                .approvalId(cardResponse.getApprovalId())
                .posOrderId(request.getPosOrderId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .status(cardResponse.getStatus())
                .cardCompany(cardResponse.getCardCompany())
                .build();

        paymentHistoryRepository.save(history);
        log.info("[VAN Gateway] 결제 이력 저장 완료 - 상태: {}, 승인번호: {}",
                cardResponse.getStatus(), cardResponse.getApprovalId());

        return cardResponse;
    }

    private PosPaymentResponse buildFailResponse(PosPaymentRequest request, String message) {
        return PosPaymentResponse.builder()
                .status("FAIL")
                .message(message)
                .posOrderId(request.getPosOrderId())
                .cardCompany("N/A")
                .build();
    }

    @Override
    public PosPaymentResponse getPaymentResult(String approvalId) {
        log.info("[VAN Gateway] 결제 결과 조회 요청 - 승인번호: {}", approvalId);
        return null;
    }
}