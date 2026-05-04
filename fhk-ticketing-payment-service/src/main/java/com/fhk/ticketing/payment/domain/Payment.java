package com.fhk.ticketing.payment.domain;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payment 테이블 역할
 * - 결제 요청 헤더
 */
@Entity
@Table(
        name = "payment_tbl",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_no", columnNames = "payment_no"),
                @UniqueConstraint(name = "uk_payment_idempotency_key", columnNames = "idempotency_key")
        },
        indexes = {
                @Index(name = "idx_payment_reservation", columnList = "reservation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "payment_seq",
            sequenceName = "payment_seq_tbl",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @Column(name = "payment_id")
    private Long id;

    // 결제 번호
    @Column(name = "payment_no", nullable = false, updatable = false, length = 50)
    private String paymentNo;

    // reservation-service 예약 id
    @Column(name = "reservation_id", nullable = false, updatable = false)
    private Long reservationId;

    // 결제 요청 멱등키
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 100)
    private String idempotencyKey;

    // 결제 요청 금액
    @Column(name = "amount", nullable = false, updatable = false)
    private int amount;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    // 결제 요청 시간
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    // 결제 승인 시간
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // 결제 실패 시간
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    protected Payment(String paymentNo, Long reservationId, String idempotencyKey, int amount, LocalDateTime now) {
        this.paymentNo = paymentNo;
        this.reservationId = reservationId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.status = PaymentStatus.REQUESTED;
        this.requestedAt = now;
    }

    /**
     * 결제 요청 생성
     */
    public static Payment request(String paymentNo, Long reservationId, String idempotencyKey, int amount, LocalDateTime now) {
        return new Payment(paymentNo, reservationId, idempotencyKey, amount, now);
    }

    /**
     * 결제 승인 처리
     */
    public void approve(LocalDateTime approvedAt) {

        // 1. 이미 승인된 결제면 그대로 반환
        if (this.status == PaymentStatus.APPROVED) {
            return;
        }

        // 2. 요청 상태의 결제인지 확인
        if (this.status != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태의 결제만 승인할 수 있습니다.");
        }

        // 3. 결제 상태 승인 처리
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
    }

    /**
     * 결제 실패 처리
     */
    public void fail(LocalDateTime failedAt) {

        // 1. 이미 실패 처리된 결제면 그대로 반환
        if (this.status == PaymentStatus.FAILED) {
            return;
        }

        // 2. 요청 상태의 결제인지 확인
        if (this.status != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태의 결제만 실패 처리할 수 있습니다.");
        }

        // 3. 결제 상태 실패 처리
        this.status = PaymentStatus.FAILED;
        this.failedAt = failedAt;
    }
}
