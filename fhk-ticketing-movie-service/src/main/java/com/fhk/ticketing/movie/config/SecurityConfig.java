package com.fhk.ticketing.movie.config;

import com.fhk.security.core.interfaces.TokenGuard;
import com.fhk.security.core.jwt.JwtVerifier;
import com.fhk.security.core.jwt.config.DefaultWhiteList;
import com.fhk.security.core.jwt.filter.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	/**
	 * [인증, 인가]
	 * JWT + Bearer Token ( JWT는 세션을 쓰지않고 클라이언트에서 토큰을 들고 api 호출시마다 토큰을 보낸다. 모바일앱에서 사용하는 api 혹은 MSA 개발시 사용된다 )
	 * <p>
	 * 어플리케이션 시작시
	 * SecurityFilterChain 객체를 Bean 으로 등록
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, JwtVerifier jwtVerifier, TokenGuard tokenGuard) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> {
					sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				})
				.authorizeHttpRequests(auth -> {

							// 화이트 리스트 = 인증에서 제외
							DefaultWhiteList.URIS.forEach(white -> {
								if (white.contains(":")) {
									String whiteUri = white.split(":")[0];
									String whiteMethod = white.split(":")[1];

									switch (whiteMethod) {
										case "OPTIONS" -> auth.requestMatchers(HttpMethod.OPTIONS, whiteUri).permitAll();
										case "POST" -> auth.requestMatchers(HttpMethod.POST, whiteUri).permitAll();
										case "GET" -> auth.requestMatchers(HttpMethod.GET, whiteUri).permitAll();
										case "PUT" -> auth.requestMatchers(HttpMethod.PUT, whiteUri).permitAll();
										case "PATCH" -> auth.requestMatchers(HttpMethod.PATCH, whiteUri).permitAll();
										case "DELETE" -> auth.requestMatchers(HttpMethod.DELETE, whiteUri).permitAll();
									}

								} else {
									auth.requestMatchers(white).permitAll();
								}
							});

							// 이 외 모든 endpoint 에 인증 수행
							auth
									.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
									.requestMatchers("/api/auth/v1/**").permitAll()
									.requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()

									.requestMatchers("/api/**").permitAll() // 일단 모든 api 인증 제외
									.anyRequest().authenticated();
						}
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.exceptionHandling(e -> {
					// AuthenticationException만 401로
					e.authenticationEntryPoint((req, res, ex) -> {
						res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
					});

					// AccessDeniedException만 403으로
					e.accessDeniedHandler((req, res, ex) -> {
						res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
					});
				})

				// jwt 필터 추가부분 ++++++
				.addFilterBefore(new JwtAuthFilter(jwtVerifier, tokenGuard), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
