package com.card.payment.van.controller;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import com.card.payment.van.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/approval") // VAN 게이트웨이임을 명시하기 위해 경로 조정
public class PaymentController {

    private final PaymentService paymentService;

    // POS 서비스로부터 결제 승인 요청을 받는 엔드포인트
    @PostMapping("/request")
    public ResponseEntity<PosPaymentResponse> approvePayment(@RequestBody PosPaymentRequest request) {
        return ResponseEntity.ok(paymentService.approvePayment(request));
    }

    // 특정 거래 ID로 결제 결과 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<PosPaymentResponse> getPaymentResult(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentResult(transactionId));
    }
}