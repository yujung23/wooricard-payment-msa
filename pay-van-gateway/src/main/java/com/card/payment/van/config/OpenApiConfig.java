package com.card.payment.van.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vanServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VAN Service API")
                        .description("카드 결제 시스템의 VAN 서비스 API 문서입니다. " +
                                "카드 결제 승인 요청 및 결제 결과 조회 기능을 제공합니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("VAN Service Team")
                                .email("van-service@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("로컬 개발 서버")
                ));
    }
}