package com.fhk.ticketing.reservation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.fhk.security.core.jwt.filter.JwtAuthFilter;
import com.fhk.security.core.jwt.config.FhkSecurityProperties;
import com.fhk.security.core.jwt.handler.JsonAccessDeniedHandler;
import com.fhk.security.core.jwt.handler.JsonAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	// security-core JWT AuthFilter 공통 인증/인가 적용
	private final JwtAuthFilter jwtAuthFilter;
	private final JsonAuthenticationEntryPoint authenticationEntryPoint;
	private final JsonAccessDeniedHandler accessDeniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, FhkSecurityProperties securityProperties) throws Exception {

		return http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)

				.sessionManagement(s -> s
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				.exceptionHandling(e -> e
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
				)

				.authorizeHttpRequests(auth -> { // 화이트리스트 application.yaml 참조
					applyWhiteList(auth, securityProperties);
					auth.anyRequest().authenticated();
				})

				.addFilterBefore(
						jwtAuthFilter,
						UsernamePasswordAuthenticationFilter.class
				)
				.build();
	}


	/**
	 * auth 객체 받아서 화이트리스트 추가하기
	 */
	private void applyWhiteList(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
			FhkSecurityProperties properties
	) {
		properties.getWhitelist().forEach(white -> {
			if (white.contains(":")) {
				String[] parts = white.split(":", 2);
				HttpMethod method = HttpMethod.valueOf(parts[0]);
				String uri = parts[1];

				auth.requestMatchers(method, uri).permitAll();
			} else {
				auth.requestMatchers(white).permitAll();
			}
		});
	}
}
