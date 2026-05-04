package com.fhk.ticketing.reservation.domain;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.reservation.enums.SeatOccupancyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SeatOccupancy 테이블 역할
 * - 현재 좌석 점유 상태
 * - HELD / CONFIRMED
 * -> 취소, 만료, 결제실패 시 row 삭제
 */
@Entity
@Table(
        name = "seat_occupancy_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_occupancy_screening_seat",
                        columnNames = {"screening_id", "seat_id"}
                )
        },
        indexes = {
                @Index(name = "idx_seat_occupancy_reservation", columnList = "reservation_id"),
                @Index(name = "idx_seat_occupancy_status_expires", columnList = "status, expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatOccupancy extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "seat_occupancy_seq",
            sequenceName = "seat_occupancy_seq_tbl",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_occupancy_seq")
    @Column(name = "seat_occupancy_id")
    private Long id;

    // reservation-service 내부 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private Reservation reservation;

    // reservation-service demo screening id
    @Column(name = "screening_id", nullable = false, updatable = false)
    private Long screeningId;

    // reservation-service demo seat id
    @Column(name = "seat_id", nullable = false, updatable = false)
    private Long seatId;

    // 좌석 점유 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SeatOccupancyStatus status;

    // HELD 상태 만료 시간
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private SeatOccupancy(
            Reservation reservation,
            Long screeningId,
            Long seatId,
            LocalDateTime expiresAt
    ) {
        this.reservation = reservation;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.expiresAt = expiresAt;
        this.status = SeatOccupancyStatus.HELD;
    }

    /**
     * 좌석 점유 생성
     */
    public static SeatOccupancy hold(
            Reservation reservation,
            Long screeningId,
            Long seatId,
            LocalDateTime expiresAt
    ) {
        return new SeatOccupancy(
                reservation,
                screeningId,
                seatId,
                expiresAt
        );
    }

    /**
     * 좌석 점유 확정 처리
     */
    public void confirm(LocalDateTime now) {
        // 1. 이미 확정된 좌석이면 그대로 반환
        if (this.status == SeatOccupancyStatus.CONFIRMED) {
            return;
        }

        // 2. 결제 대기 상태인지 확인
        if (this.status != SeatOccupancyStatus.HELD) {
            throw new IllegalStateException("결제 대기 상태의 좌석만 확정할 수 있습니다.");
        }

        // 3. 점유 시간이 만료됐는지 확인
        if (isExpired(now)) {
            throw new IllegalStateException("이미 만료된 좌석 점유입니다.");
        }

        // 4. 좌석 점유 확정 처리
        this.status = SeatOccupancyStatus.CONFIRMED;
        this.expiresAt = null;
    }

    /**
     * 결제 대기 점유 상태 여부 확인
     */
    public boolean isHeld() {
        return this.status == SeatOccupancyStatus.HELD;
    }

    /**
     * 확정 점유 상태 여부 확인
     */
    public boolean isConfirmed() {
        return this.status == SeatOccupancyStatus.CONFIRMED;
    }

    /**
     * 좌석 점유 만료 여부 확인
     */
    public boolean isExpired(LocalDateTime now) {
        return this.status == SeatOccupancyStatus.HELD
                && this.expiresAt != null
                && this.expiresAt.isBefore(now);
    }

    /**
     * 동일 상영 좌석 여부 확인
     */
    public boolean isSameSeat(Long screeningId, Long seatId) {
        return this.screeningId.equals(screeningId)
                && this.seatId.equals(seatId);
    }
}
