package com.fhk.ticketing.movie.domain.movie;

import com.fhk.ticketing.movie.common.movie.MovieCreditRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * 출연진
 *
 * - id
 *
 * - 한글 성명
 * - 영문 성명
 *
 * - 역할 ( 감독, 주연, 조연 )
 *
 */
@Entity
public class MovieCredit {

	@Id
	@Column(name = "movie_credit_id")
	private Long id;

	@Column(name = "movie_credit_name_ko")
	private String nameKo;

	@Column(name = "movie_credit_name_en")
	private String nameEn;

	@Column(name = "movie_credit_role")
	private MovieCreditRole role;
}
