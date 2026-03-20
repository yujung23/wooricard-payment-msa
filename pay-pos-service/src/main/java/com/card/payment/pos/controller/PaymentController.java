package com.card.payment.pos.controller;

import com.card.payment.pos.dto.PaymentRequest;
import com.card.payment.pos.dto.PaymentResponse;
import com.card.payment.pos.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/approval")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> requestPayment(@RequestBody PaymentRequest request){
        return ResponseEntity.ok(paymentService.requestPayment(request));
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<PaymentResponse> getPaymentResult(@PathVariable String approvalId) {
        return ResponseEntity.ok(paymentService.getPaymentResult(approvalId));
    }
}
