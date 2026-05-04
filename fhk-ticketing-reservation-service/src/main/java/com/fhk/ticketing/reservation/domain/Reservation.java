package com.fhk.ticketing.reservation.domain;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.reservation.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reservation 테이블 역할
 * - 예약 헤더
 */
@Entity
@Table(
        name = "reservation_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_no",
                        columnNames = "reservation_no"
                )
        },
        indexes = {
                @Index(name = "idx_reservation_account_reg_time", columnList = "account_id, reg_time"),
                @Index(name = "idx_reservation_screening_status", columnList = "screening_id, status"),
                @Index(name = "idx_reservation_status_expires", columnList = "status, expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "reservation_seq",
            sequenceName = "reservation_seq_tbl",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reservation_seq")
    @Column(name = "reservation_id")
    private Long id;

    // 예매 번호 ex) RSV-20260425-000001
    @Column(name = "reservation_no", nullable = false, updatable = false, length = 50)
    private String reservationNo;

    // security-server account id
    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    // reservation-service demo screening id
    @Column(name = "screening_id", nullable = false, updatable = false)
    private Long screeningId;

    // 예약 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    // 예매 당시 총 금액
    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    // 결제 대기 만료 시간
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // 결제 성공 후 예매 확정 시간
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    // 사용자 취소 시간
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    private Reservation(
            String reservationNo,
            Long accountId,
            Long screeningId,
            int totalAmount,
            LocalDateTime expiresAt
    ) {
        this.reservationNo = reservationNo;
        this.accountId = accountId;
        this.screeningId = screeningId;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.PENDING_PAYMENT;
    }

    /**
     * 결제 대기 예약 생성
     */
    public static Reservation createPending(
            String reservationNo,
            Long accountId,
            Long screeningId,
            int totalAmount,
            LocalDateTime expiresAt
    ) {
        return new Reservation(
                reservationNo,
                accountId,
                screeningId,
                totalAmount,
                expiresAt
        );
    }

    /**
     * 예약 확정 처리
     */
    public void confirm(LocalDateTime now) {
        // 1. 결제 대기 상태인지 확인
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태의 예약만 확정할 수 있습니다.");
        }

        // 2. 결제 대기 시간이 만료됐는지 확인
        if (this.expiresAt.isBefore(now)) {
            throw new IllegalStateException("결제 대기 시간이 만료된 예약입니다.");
        }

        // 3. 예약 상태 확정 처리
        this.status = ReservationStatus.RESERVED;
        this.reservedAt = now;
    }

    /**
     * 예약 취소 처리
     */
    public void cancel(LocalDateTime now) {
        // 1. 취소 가능한 예약 상태인지 확인
        if (this.status != ReservationStatus.PENDING_PAYMENT
                && this.status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다.");
        }

        // 2. 예약 상태 취소 처리
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = now;
    }

    /**
     * 결제 대기 만료 처리
     */
    public void expire(LocalDateTime now) {
        // 1. 결제 대기 상태인지 확인
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태의 예약만 만료 처리할 수 있습니다.");
        }

        // 2. 예약 상태 만료 처리
        this.status = ReservationStatus.EXPIRED;
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(LocalDateTime now) {
        // 1. 결제 대기 상태인지 확인
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태의 예약만 결제 실패 처리할 수 있습니다.");
        }

        // 2. 예약 상태 결제 실패 처리
        this.status = ReservationStatus.PAYMENT_FAILED;
    }

    /**
     * 결제 대기 시간 만료 여부 확인
     */
    public boolean isExpired(LocalDateTime now) {
        return this.expiresAt.isBefore(now);
    }

    /**
     * 결제 대기 상태 여부 확인
     */
    public boolean isPendingPayment() {
        return this.status == ReservationStatus.PENDING_PAYMENT;
    }

    /**
     * 예약 확정 상태 여부 확인
     */
    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }
}
