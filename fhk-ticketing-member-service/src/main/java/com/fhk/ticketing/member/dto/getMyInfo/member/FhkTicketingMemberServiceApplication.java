package com.fhk.ticketing.member.dto.getMyInfo.member;

import com.fhk.common.exception.GlobalExceptionHandler;
import com.fhk.security.core.interfaces.TokenGuard;
import com.fhk.security.core.jwt.JwtVerifier;
import com.fhk.security.core.jwt.config.JwtVerifierProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@SpringBootApplication
@SpringBootApplication
@EnableJpaAuditing // BaseEntity
@EnableConfigurationProperties(JwtVerifierProperties.class) // JwtProperties
@Import({GlobalExceptionHandler.class, JwtVerifier.class, TokenGuard.class}) // 임포트    @ComponentScan("com.fhk.security.core")      @SpringBootApplication

public class FhkTicketingMemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhkTicketingMemberServiceApplication.class, args);
	}

}
