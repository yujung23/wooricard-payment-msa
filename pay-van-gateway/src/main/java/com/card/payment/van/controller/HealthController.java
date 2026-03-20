package com.card.payment.van.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "헬스체크", description = "서비스 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(
            summary = "서비스 상태 확인",
            description = "VAN 서비스의 정상 작동 여부를 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "서비스 정상",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                    {
                      "status": "UP",
                      "service": "VAN Service"
                    }
                    """
                    )
            )
    )
    @GetMapping
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "VAN Service");
        return response;
    }
}