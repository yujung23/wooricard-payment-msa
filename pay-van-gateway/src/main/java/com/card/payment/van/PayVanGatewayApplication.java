package com.card.payment.van;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PayVanGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayVanGatewayApplication.class, args);
	}

}
