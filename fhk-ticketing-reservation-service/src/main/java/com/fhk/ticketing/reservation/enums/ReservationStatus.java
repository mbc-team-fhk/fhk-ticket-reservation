package com.fhk.ticketing.reservation.enums;

/**
 * 예약 상태
 */
public enum ReservationStatus {
    PENDING_PAYMENT,    // 좌석 점유 완료, 결제 대기
    RESERVED,           // 결제 성공, 예매 확정
    CANCELED,           // 사용자 취소
    EXPIRED,            // 결제 대기 시간 만료
    PAYMENT_FAILED,     // 결제 실패
}
