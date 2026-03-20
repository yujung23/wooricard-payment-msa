package com.card.payment.van.client;

import com.card.payment.van.dto.CardAuthorizationRequest;
import com.card.payment.van.dto.CardAuthorizationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CARD-AUTHORIZATION-SERVICE")
public interface CardAuthorizationClient {

    @PostMapping("/api/authorization/request")
    CardAuthorizationResponse requestAuthorization(@RequestBody CardAuthorizationRequest request);
}
