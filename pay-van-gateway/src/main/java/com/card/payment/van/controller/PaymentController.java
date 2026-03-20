package com.card.payment.van.controller;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import com.card.payment.van.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/van/approval") // VAN 게이트웨이임을 명시하기 위해 경로 조정
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인 요청", description = "POS 서비스로부터 카드 결제 승인 요청을 수신하여 카드사로 전달합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "승인 처리 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PosPaymentResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "1. 일시불 승인 요청",
                                            description = "일시불 정상 승인 요청",
                                            value = """
                                    {
                                      "cardNumber": "4123456789012345",
                                      "expiryDate": "2028-12",
                                      "amount": 50000,
                                      "posOrderId": "ORDER_001",
                                      "merchantId": "MERCHANT_001"
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "2. 고액 결제 요청",
                                            description = "고액 결제 승인 요청",
                                            value = """
                                    {
                                      "cardNumber": "4123456789012345",
                                      "expiryDate": "2028-12",
                                      "amount": 300000,
                                      "posOrderId": "ORDER_002",
                                      "merchantId": "MERCHANT_001"
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 카드사 연결 실패")
    })
    // POS 서비스로부터 결제 승인 요청을 받는 엔드포인트
    @PostMapping("/request")
    public ResponseEntity<PosPaymentResponse> approvePayment(@RequestBody PosPaymentRequest request) {
        return ResponseEntity.ok(paymentService.approvePayment(request));
    }

    @Operation(summary = "결제 결과 조회", description = "거래 ID로 결제 승인 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PosPaymentResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                              "approvalId": "APR20260320001",
                              "status": "APPROVED",
                              "message": "승인완료",
                              "approvedAt": "2026-03-20T10:30:00",
                              "cardCompany": "SHINHAN",
                              "posOrderId": "ORDER_001"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    // 특정 거래 ID로 결제 결과 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<PosPaymentResponse> getPaymentResult(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentResult(transactionId));
    }
}