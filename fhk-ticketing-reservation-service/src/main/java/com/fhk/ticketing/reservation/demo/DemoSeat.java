package com.fhk.ticketing.reservation.demo;

/**
 * 데모 좌석 정보
 */
public record DemoSeat(
        Long seatId,
        String seatRow,
        Integer seatNumber,
        String seatLabel,
        int price
) {
}
