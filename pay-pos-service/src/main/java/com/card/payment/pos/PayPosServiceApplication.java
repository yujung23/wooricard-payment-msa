package com.card.payment.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PayPosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayPosServiceApplication.class, args);
	}

}
