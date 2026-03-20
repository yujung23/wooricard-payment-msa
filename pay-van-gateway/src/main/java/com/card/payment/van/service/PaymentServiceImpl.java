package com.card.payment.van.service;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import com.card.payment.van.entity.PaymentHistory; // 추가
import com.card.payment.van.repository.PaymentHistoryRepository; // 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 추가

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository; // [수정 1] 리포지토리 주입

    @Override
    @Transactional // [수정 2] DB 저장을 위해 트랜잭션 보장
    public PosPaymentResponse approvePayment(PosPaymentRequest request) {
        log.info("[VAN Gateway] 승인 요청 수신 - POS 주문번호: {}, 결제금액: {}",
                request.getPosOrderId(), request.getAmount());

        // 1. 승인 번호 생성
        String generatedApprovalId = "VAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. DB 저장을 위한 엔티티 생성 및 저장 [수정 3]
        PaymentHistory history = PaymentHistory.builder()
                .approvalId(generatedApprovalId)
                .posOrderId(request.getPosOrderId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .cardCompany("WOORICARD")
                .build();

        paymentHistoryRepository.save(history); // 실제 DB에 저장됨!
        log.info("[VAN Gateway] 결제 이력 저장 완료 - 승인번호: {}", generatedApprovalId);

        // 3. 응답 반환
        return PosPaymentResponse.builder()
                .approvalId(generatedApprovalId)
                .status("SUCCESS")
                .message("정상 승인되었습니다.")
                .approvedAt(LocalDateTime.now())
                .cardCompany("WOORICARD")
                .posOrderId(request.getPosOrderId())
                .build();
    }

    @Override
    public PosPaymentResponse getPaymentResult(String approvalId) {
        log.info("[VAN Gateway] 결제 결과 조회 - 승인번호: {}", approvalId);

        return null;

    }
}