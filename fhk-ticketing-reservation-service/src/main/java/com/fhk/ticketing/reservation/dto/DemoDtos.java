package com.fhk.ticketing.reservation.dto;

import com.fhk.ticketing.reservation.demo.DemoScreening;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 데모 상영 API 응답 DTO 모음
 */
public class DemoDtos {

    /**
     * 데모 상영 목록 응답
     */
    public record ScreeningSummaryRes(
            Long screeningId,
            String movieTitle,
            String cinemaName,
            String screenRoomName,
            LocalDateTime startsAt
    ) {
        /**
         * 데모 상영 목록 응답 변환
         */
        public static ScreeningSummaryRes from(DemoScreening screening) {
            return new ScreeningSummaryRes(
                    screening.screeningId(),
                    screening.movieTitle(),
                    screening.cinemaName(),
                    screening.screenRoomName(),
                    screening.startsAt()
            );
        }
    }

    /**
     * 데모 상영 상세 응답
     */
    public record ScreeningDetailRes(
            Long screeningId,
            String movieTitle,
            String cinemaName,
            String screenRoomName,
            LocalDateTime startsAt,
            List<SeatSnapshotRes> seats
    ) {
        /**
         * 데모 상영 상세 응답 변환
         */
        public static ScreeningDetailRes from(DemoScreening screening) {
            // 1. 좌석 목록 응답 형태로 변환
            List<SeatSnapshotRes> seats = screening.seats().stream()
                    .map(seat -> new SeatSnapshotRes(
                            seat.seatId(),
                            seat.seatRow(),
                            seat.seatNumber(),
                            seat.seatLabel(),
                            seat.price()
                    ))
                    .toList();

            // 2. 상영 기본 정보와 좌석 목록 조합
            return new ScreeningDetailRes(
                    screening.screeningId(),
                    screening.movieTitle(),
                    screening.cinemaName(),
                    screening.screenRoomName(),
                    screening.startsAt(),
                    seats
            );
        }
    }

    /**
     * 데모 좌석 스냅샷 응답
     */
    public record SeatSnapshotRes(
            Long seatId,
            String seatRow,
            Integer seatNumber,
            String seatLabel,
            int price
    ) {
    }
}
