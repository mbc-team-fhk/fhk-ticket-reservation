package com.fhk.ticketing.reservation.domain;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.reservation.enums.ReservationSeatStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ReservationSeat 테이블 역할
 * - 예약 좌석 상세
 */
@Entity
@Table(
        name = "reservation_seat_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_seat_reservation_seat",
                        columnNames = {"reservation_id", "seat_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_reservation_seat_reservation",
                        columnList = "reservation_id"
                ),
                @Index(
                        name = "idx_reservation_seat_screening_seat",
                        columnList = "screening_id, seat_id"
                ),
                @Index(
                        name = "idx_reservation_seat_status",
                        columnList = "status"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "reservation_seat_seq",
            sequenceName = "reservation_seat_seq_tbl",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reservation_seat_seq")
    @Column(name = "reservation_seat_id")
    private Long id;

    /**
     * reservation-service 내부 예약 FK
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private Reservation reservation;

    /**
     * reservation-service demo screening id
     */
    @Column(name = "screening_id", nullable = false, updatable = false)
    private Long screeningId;

    /**
     * reservation-service demo seat id
     */
    @Column(name = "seat_id", nullable = false, updatable = false)
    private Long seatId;

    /**
     * 예매 당시 좌석 행 스냅샷
     * 예: A, B, H
     */
    @Column(name = "seat_row", nullable = false, updatable = false, length = 20)
    private String seatRow;

    /**
     * 예매 당시 좌석 번호 스냅샷
     * 예: 1, 2, 10
     */
    @Column(name = "seat_number", nullable = false, updatable = false)
    private Integer seatNumber;

    /**
     * 화면 표시용 좌석명 스냅샷
     * 예: H열 2번
     */
    @Column(name = "seat_label", nullable = false, updatable = false, length = 50)
    private String seatLabel;

    /**
     * 예매 당시 좌석 가격 스냅샷
     */
    @Column(name = "price", nullable = false, updatable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationSeatStatus status;

    private ReservationSeat(
            Reservation reservation,
            Long screeningId,
            Long seatId,
            String seatRow,
            Integer seatNumber,
            String seatLabel,
            int price
    ) {
        this.reservation = reservation;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatLabel = seatLabel;
        this.price = price;
        this.status = ReservationSeatStatus.HELD;
    }

    public static ReservationSeat hold(
            Reservation reservation,
            Long screeningId,
            Long seatId,
            String seatRow,
            Integer seatNumber,
            String seatLabel,
            int price
    ) {
        return new ReservationSeat(
                reservation,
                screeningId,
                seatId,
                seatRow,
                seatNumber,
                seatLabel,
                price
        );
    }

    public void confirm() {
        if (this.status == ReservationSeatStatus.CONFIRMED) {
            return;
        }

        if (this.status != ReservationSeatStatus.HELD) {
            throw new IllegalStateException("결제 대기 상태의 좌석만 확정할 수 있습니다.");
        }

        this.status = ReservationSeatStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationSeatStatus.CANCELED) {
            return;
        }

        if (this.status != ReservationSeatStatus.HELD
                && this.status != ReservationSeatStatus.CONFIRMED) {
            throw new IllegalStateException("취소할 수 없는 예약 좌석 상태입니다.");
        }

        this.status = ReservationSeatStatus.CANCELED;
    }

    public void expire() {
        if (this.status == ReservationSeatStatus.EXPIRED) {
            return;
        }

        if (this.status != ReservationSeatStatus.HELD) {
            throw new IllegalStateException("결제 대기 상태의 좌석만 만료 처리할 수 있습니다.");
        }

        this.status = ReservationSeatStatus.EXPIRED;
    }

    public boolean isHeld() {
        return this.status == ReservationSeatStatus.HELD;
    }

    public boolean isConfirmed() {
        return this.status == ReservationSeatStatus.CONFIRMED;
    }

    public boolean isCanceled() {
        return this.status == ReservationSeatStatus.CANCELED;
    }

    public boolean isExpired() {
        return this.status == ReservationSeatStatus.EXPIRED;
    }

    public boolean isSameSeat(Long screeningId, Long seatId) {
        return this.screeningId.equals(screeningId)
                && this.seatId.equals(seatId);
    }
}
