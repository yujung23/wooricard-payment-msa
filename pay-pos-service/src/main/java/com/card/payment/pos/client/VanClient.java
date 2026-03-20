package com.card.payment.pos.client;

import com.card.payment.pos.dto.PaymentRequest;
import com.card.payment.pos.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAY-VAN-GATEWAY")
public interface VanClient {

    @PostMapping("/api/v1/approval/request")
    PaymentResponse requestApproval(@RequestBody PaymentRequest request);
}
