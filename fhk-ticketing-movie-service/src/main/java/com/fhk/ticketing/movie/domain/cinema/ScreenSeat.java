package com.fhk.ticketing.movie.domain.cinema;

/**
 * 좌석
 *
 * - id
 *
 * - 스크린 ID (최소 screening 생성시 base seat 참조용)
 *
 * - 상영 ID (상영 기준으로 base seat 를 복제하여 사용)
 *
 * - y:column ( 1~28... )
 * - x:row code ( A~P... )
 *
 * - 좌석 상태 ( block, open, lock, closed )
 *
 * - 좌석 타입 ( 일반석, 장애인석 )
 *
 */
public class ScreenSeat {

}
