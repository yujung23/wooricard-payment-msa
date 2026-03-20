package com.card.payment.van.client;

import com.card.payment.van.dto.PosPaymentRequest;
import com.card.payment.van.dto.PosPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 상대방의 VanClient 구조를 따른 인터페이스 방식
@FeignClient(name = "PAY-POS-SERVICE") // 호출할 서비스 이름 (Eureka 등록 명칭)
public interface CardClient {

    @PostMapping("/api/v1/card/approve")
    PosPaymentResponse requestCardApproval(@RequestBody PosPaymentRequest request);
}