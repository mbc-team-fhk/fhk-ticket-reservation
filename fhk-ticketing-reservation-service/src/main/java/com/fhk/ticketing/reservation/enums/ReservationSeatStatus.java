package com.fhk.ticketing.reservation.enums;

/**
 * 예약 좌석 상태
 */
public enum ReservationSeatStatus {
    HELD,       // 결제 대기 좌석
    CONFIRMED,  // 예매 확정 좌석
    CANCELED,   // 취소 좌석
    EXPIRED     // 결제 대기 만료 좌석
}
