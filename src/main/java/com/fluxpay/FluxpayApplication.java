package com.fluxpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FluxpayApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluxpayApplication.class, args);
	}

}
