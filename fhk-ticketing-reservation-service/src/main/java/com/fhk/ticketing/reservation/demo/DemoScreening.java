package com.fhk.ticketing.reservation.demo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 데모 상영 정보
 */
public record DemoScreening(
        Long screeningId,
        String movieTitle,
        String cinemaName,
        String screenRoomName,
        LocalDateTime startsAt,
        List<DemoSeat> seats
) {
}
