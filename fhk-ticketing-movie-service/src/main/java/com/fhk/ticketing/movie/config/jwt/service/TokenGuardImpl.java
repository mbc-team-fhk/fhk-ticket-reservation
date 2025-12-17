package com.fhk.ticketing.movie.config.jwt.service;

import com.fhk.security.core.interfaces.TokenGuard;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@AllArgsConstructor
public class TokenGuardImpl implements TokenGuard {

	private final StringRedisTemplate redisTemplate;

	/**
	 * 토큰 버전 검증
	 *
	 * 각 서버는
	 * redis cache 확인만 하고 Exception 처리
	 *
	 * @param uid accountId
	 * @param tokenVersion
	 */
	private void checkVersion(long uid, Integer tokenVersion) {
		String redisKey = "fhk:security:account:" + uid + ":ver";
		String redisValue = redisTemplate.opsForValue().get(redisKey);

		if (redisValue == null) {
			throw new CredentialsExpiredException("no version info in cache");
		}

		int currentVersion = Integer.parseInt(redisValue);
		if (tokenVersion == null || !tokenVersion.equals(currentVersion)) {
			throw new CredentialsExpiredException("ver mismatch");
		}
	}

	@Override
	public void verifyAccess(Claims claims) {
		long uid = Long.parseLong(claims.getSubject());
		Integer tokenVersion = claims.get("version", Integer.class);

		// aud 검증
		if (!"access".equals(claims.getAudience())) {
			throw new BadCredentialsException("not access token");
		}

		// token 내 만료기간 없음
		Date expDate = claims.getExpiration();
		if (expDate == null)
			throw new BadCredentialsException("exp missing");

		checkVersion(uid, tokenVersion);
	}

	@Override
	public void verifyRefresh(Claims claims) {
		long uid = Long.parseLong(claims.getSubject());
		Integer tokenVersion = claims.get("version", Integer.class);

		// aud == refresh 검증
		if (!"refresh".equals(claims.getAudience())) {
			throw new BadCredentialsException("not refresh token");
		}

		// token 내 만료기간 없음
		Date expDate = claims.getExpiration();
		if (expDate == null)
			throw new BadCredentialsException("exp missing");

		checkVersion(uid, tokenVersion);
	}
}
