package com.card.payment.van.service;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PosPaymentResponse approvePayment(PosPaymentRequest request) {
        log.info("[VAN Gateway] 승인 요청 수신 - POS 주문번호: {}, 결제금액: {}",
                request.getPosOrderId(), request.getAmount());

        // 1. 외부 VAN사 API 호출 (현재는 Mock 데이터로 응답 생성)
        // 상대방(POS)이 기대하는 'approvalId', 'status', 'cardCompany' 필드를 정확히 채워줍니다.
        String generatedApprovalId = "VAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PosPaymentResponse.builder()
                .approvalId(generatedApprovalId)      // POS 담당자가 'response.approvalId()'로 읽음
                .status("SUCCESS")                    // POS 담당자가 'response.status()'로 읽음
                .message("정상 승인되었습니다.")         // POS 담당자가 'response.message()'로 읽음
                .approvedAt(LocalDateTime.now())      // POS 담당자가 'response.approvedAt()'으로 읽음
                .cardCompany("WOORICARD")             // POS 담당자가 'response.cardCompany()'로 읽음
                .posOrderId(request.getPosOrderId())  // 내부 관리용
                .build();
    }

    @Override
    public PosPaymentResponse getPaymentResult(String approvalId) {
        log.info("[VAN Gateway] 결제 결과 조회 - 승인번호: {}", approvalId);

        // TODO: 나중에 Repository를 생성하면 DB에서 조회하는 로직을 구현합니다.
        return null;
    }
}