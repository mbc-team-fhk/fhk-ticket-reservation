package com.fhk.ticketing.movie.domain.cinema;

/**
 * 상영 관리자는
 * 1. 상영관 선정 ( 스크린 타입 고정 = 청소시간 등 )
 * 2. 영화 선정
 * 3. 시작 시간 선정
 *
 *
 * 영화 상영정보
 *
 * - id
 *
 * - 영화 ID ( Movie Service api 에서 참조 )
 * - 상영관 ID ( ScreenRoom Repo join )
 *
 * - 남은좌석 ( Reservation Service -> kafka 연동 )
 *
 */
public class Screening {

}
