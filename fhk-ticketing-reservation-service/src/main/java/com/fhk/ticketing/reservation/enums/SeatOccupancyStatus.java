package com.fhk.ticketing.reservation.enums;

/**
 * 현재 좌석 점유 상태
 */
public enum SeatOccupancyStatus {
    HELD,       // 결제 대기 중 임시 점유
    CONFIRMED,  // 결제 성공 후 확정 점유
}
