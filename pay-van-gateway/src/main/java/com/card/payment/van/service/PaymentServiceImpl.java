package com.card.payment.van.service;

import com.card.payment.van.client.CardAuthorizationClient;
import com.card.payment.van.dto.CardAuthorizationRequest;
import com.card.payment.van.dto.CardAuthorizationResponse;
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
    private final CardAuthorizationClient cardAuthorizationClient;

    @Override
    @Transactional
    public PosPaymentResponse approvePayment(PosPaymentRequest request) {
        log.info("[VAN Gateway] 승인 요청 수신 - POS 주문번호: {}, 결제금액: {}",
                request.posOrderId(), request.transactionAmount());

        // 1. POS 요청 → 카드사 요청으로 변환 (STAN은 CardAuthorizationRequest.from() 내부에서 생성)
        CardAuthorizationRequest authRequest = CardAuthorizationRequest.from(request);

        // 2. 카드사 호출
        log.info("[VAN Gateway] 카드사 승인 요청 - STAN: {}", authRequest.transactionId());
        CardAuthorizationResponse authResponse = cardAuthorizationClient.requestAuthorization(authRequest);

        // 3. 결제 이력 저장 (카드사 응답 기반)
        PaymentHistory history = PaymentHistory.builder()
                .approvalId(authResponse.transactionId())
                .posOrderId(request.posOrderId())
                .merchantId(request.cardAcceptorId())
                .amount(request.transactionAmount())
                .status(authResponse.responseCode())
                .cardCompany(resolveCardCompany(request.primaryAccountNumber()))
                .build();

        paymentHistoryRepository.save(history);
        log.info("[VAN Gateway] 결제 이력 저장 완료 - STAN: {}, responseCode: {}",
                authResponse.transactionId(), authResponse.responseCode());

        // 4. 카드사 응답 → POS 응답으로 변환 (cardCompany는 VAN이 BIN으로 판단)
        return PosPaymentResponse.from(authResponse, request.posOrderId(), resolveCardCompany(request.primaryAccountNumber()));
    }

    @Override
    public PosPaymentResponse getPaymentResult(String approvalId) {
        log.info("[VAN Gateway] 결제 결과 조회 - 승인번호: {}", approvalId);
        return null;
    }

    /**
     * BIN 번호 기반 카드사 식별
     * 카드번호 앞자리로 카드사를 유추합니다.
     */
    private String resolveCardCompany(String primaryAccountNumber) {
        if (primaryAccountNumber == null || primaryAccountNumber.isEmpty()) {
            return "UNKNOWN";
        }
        return switch (primaryAccountNumber.charAt(0)) {
            case '4' -> "VISA";
            case '5' -> "MASTERCARD";
            case '9' -> "WOORICARD";
            default -> "UNKNOWN";
        };
    }
}