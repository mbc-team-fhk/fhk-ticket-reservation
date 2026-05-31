package com.fhk.ticketing.payment;

import com.fhk.common.exception.GlobalExceptionHandler;
import com.fhk.common.logging.ApiBodyLoggingFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // BaseEntity
@EnableScheduling
@Import({GlobalExceptionHandler.class, ApiBodyLoggingFilter.class})
public class FhkTicketingPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhkTicketingPaymentServiceApplication.class, args);
	}

}
