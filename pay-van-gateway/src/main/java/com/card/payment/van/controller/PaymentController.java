package com.card.payment.van.controller;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import com.card.payment.van.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "VAN Payment", description = "VAN 게이트웨이 결제 승인 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "결제 승인 요청",
            description = "POS 서비스로부터 카드 결제 승인 요청을 수신하여 카드사로 전달합니다. 승인번호(STAN, DE11)는 VAN에서 생성되며 응답의 systemTraceAuditNumber로 반환됩니다. (현재 구현은 UUID 형식)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "승인 처리 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PosPaymentResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "systemTraceAuditNumber": "550e8400-e29b-41d4-a716-446655440000",
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
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 카드사 연결 실패")
    })
    @PostMapping("/request")
    public ResponseEntity<PosPaymentResponse> approvePayment(
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
            @RequestBody PosPaymentRequest request) {
        return ResponseEntity.ok(paymentService.approvePayment(request));
    }

    @Operation(summary = "결제 결과 조회", description = "거래 고유번호(STAN)로 승인 내역을 조회합니다.")
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
                              "systemTraceAuditNumber": "550e8400-e29b-41d4-a716-446655440000",
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
    public ResponseEntity<PosPaymentResponse> getPaymentResult(String stan) {
        return ResponseEntity.ok(paymentService.getPaymentResult(stan));
    }
}
