package com.fhk.ticketing.reservation.dto;

import com.fhk.ticketing.reservation.domain.Reservation;
import com.fhk.ticketing.reservation.domain.ReservationSeat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 API 요청/응답 DTO 모음
 */
public class ReservationDtos {

    /**
     * 예약 생성 요청
     */
    public record CreateReservationReq(
            @NotNull Long screeningId,
            @NotEmpty List<@NotNull Long> seatIds
    ) {
    }

    /**
     * 예약 상세 응답
     */
    public record ReservationRes(
            Long reservationId,
            String reservationNo,
            Long screeningId,
            String status,
            int totalAmount,
            LocalDateTime expiresAt,
            LocalDateTime reservedAt,
            LocalDateTime canceledAt,
            List<ReservationSeatRes> seats
    ) {
        /**
         * 예약 상세 응답 변환
         */
        public static ReservationRes from(Reservation reservation, List<ReservationSeat> seats) {
            // 1. 예약 좌석 목록 응답 형태로 변환
            List<ReservationSeatRes> seatResponses = seats.stream()
                    .map(ReservationSeatRes::from)
                    .toList();

            // 2. 예약 기본 정보와 좌석 목록 조합
            return new ReservationRes(
                    reservation.getId(),
                    reservation.getReservationNo(),
                    reservation.getScreeningId(),
                    reservation.getStatus().name(),
                    reservation.getTotalAmount(),
                    reservation.getExpiresAt(),
                    reservation.getReservedAt(),
                    reservation.getCanceledAt(),
                    seatResponses
            );
        }
    }

    /**
     * 예약 좌석 응답
     */
    public record ReservationSeatRes(
            Long seatId,
            String seatRow,
            Integer seatNumber,
            String seatLabel,
            int price,
            String status
    ) {
        /**
         * 예약 좌석 응답 변환
         */
        public static ReservationSeatRes from(ReservationSeat seat) {
            return new ReservationSeatRes(
                    seat.getSeatId(),
                    seat.getSeatRow(),
                    seat.getSeatNumber(),
                    seat.getSeatLabel(),
                    seat.getPrice(),
                    seat.getStatus().name()
            );
        }
    }

    /**
     * 상영 좌석 상태 응답
     */
    public record SeatStatusRes(
            Long screeningId,
            List<SeatStatusItem> seats
    ) {
    }

    /**
     * 상영 좌석 상태 항목
     */
    public record SeatStatusItem(
            Long seatId,
            String seatRow,
            Integer seatNumber,
            String seatLabel,
            int price,
            String status,
            Long reservationId,
            LocalDateTime expiresAt
    ) {
    }

    /**
     * 결제 결과 반영 요청
     */
    public record PaymentResultReq(
            String paymentNo,
            String resultStatus,
            int amount,
            String callbackEventId
    ) {
    }
}
