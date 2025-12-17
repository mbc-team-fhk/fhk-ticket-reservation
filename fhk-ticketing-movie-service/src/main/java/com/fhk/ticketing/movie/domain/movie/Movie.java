package com.fhk.ticketing.movie.domain.movie;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.movie.common.movie.MovieAge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * 영화
 *
 * - id
 *
 * - 영화코드
 * - 타이틀
 * - 개봉일(예정일)
 * - 러닝타임(상영시간)
 * - 시청연령
 *
 * - 좋아요수
 * - 관람객수 ( KOBIS 통합집계 ? )
 *
 * - 영화 설명
 * - 장르
 * - 국가
 * - 감독
 * - 출연자
 * - 트레일러 목록 (영상이름, mp4_url)
 * - 스틸컷 목록 (이미지 img_url)
 *
 */
@Entity
@Table(name = "movie_tbl")
@Getter
@Setter
@ToString
public class Movie extends BaseEntity {

	@Id
	@Column(name = "movie_id")
	private Long id;

	// 기본 정보 -------------------------------
	@Column(name = "movie_cd")
	private Long movieCode;

	@Column(name = "movie_title_ko")
	private String titleKo;

	@Column(name = "movie_title_en")
	private String titleEn;

	@Column(name = "movie_running_time")
	private int runningTime;

	@Column(name = "movie_age")
	private MovieAge age;

	// 통계/카운트 -------------------------------
	@Column(name = "movie_like_feed")
	private int likeFeed; // kafka 좋아요

	@Column(name = "movie_audience")
	private int audience; // kafka 관람객 수 (추후 일별, 총합)

	// 설명 -------------------------------
	private String description;

	@ManyToMany
	@JoinTable(
			name = "movie_genres",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "genre_id")
	)
	private List<Genre> genres; // 복수 장르

	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> countries; // 복수 국가

	@ManyToMany
	@JoinTable(
			name = "movie_credits",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "movie_credit_id")
	)
	private List<MovieCredit> credits; // 감독. 주요 출연진 등
}
