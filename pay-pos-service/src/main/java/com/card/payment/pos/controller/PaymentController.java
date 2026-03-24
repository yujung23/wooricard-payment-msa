package com.card.payment.pos.controller;

import com.card.payment.pos.dto.PaymentRequest;
import com.card.payment.pos.dto.PaymentResponse;
import com.card.payment.pos.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/approval")
@Tag(name = "Payment", description = "POS 결제 승인 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인 요청", description = "POS에서 VAN 서버로 카드 결제 승인을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "승인 처리 완료 (승인 또는 거절)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "승인 성공",
                                            value = """
                                    {
                                      "systemTraceAuditNumber": "APR20260319001",
                                      "responseCode": "00",
                                      "responseMessage": "승인완료",
                                      "approvedAt": "2026-03-19T10:30:00",
                                      "cardCompany": "WOORICARD",
                                      "posOrderId": "POS-ORDER-001"
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 VAN 서비스 연결 실패")
    })
    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> requestPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "결제 승인 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "primaryAccountNumber": "4111111111111111",
                                      "expirationDate": "2028-12",
                                      "transactionAmount": 50000,
                                      "cardAcceptorId": "MERCHANT_001",
                                      "terminalId": "TERMINAL_001",
                                      "installmentMonths": 0,
                                      "posOrderId": "POS-ORDER-001"
                                    }
                                    """
                            )
                    )
            )
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.requestPayment(request));
    }

    @Operation(summary = "승인 결과 조회", description = "거래 고유번호(STAN)로 승인 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                              "systemTraceAuditNumber": "APR20260319001",
                              "responseCode": "00",
                              "responseMessage": "승인완료",
                              "approvedAt": "2026-03-19T10:30:00",
                              "cardCompany": "WOORICARD",
                              "posOrderId": "POS-ORDER-001"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{stan}")
    public ResponseEntity<PaymentResponse> getPaymentResult(@PathVariable String stan) {
        return ResponseEntity.ok(paymentService.getPaymentResult(stan));
    }
}