package com.fhk.ticketing.payment.domain;

import com.fhk.core.entity.BaseEntity;
import com.fhk.ticketing.payment.enums.CallbackProcessStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentCallbackLog 테이블 역할
 * - PG 콜백 처리 이력
 */
@Entity
@Table(
        name = "payment_callback_log_tbl",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_callback_event", columnNames = "callback_event_id")
        },
        indexes = {
                @Index(name = "idx_payment_callback_payment", columnList = "payment_id"),
                @Index(name = "idx_payment_callback_payment_no", columnList = "payment_no")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCallbackLog extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "payment_callback_log_seq",
            sequenceName = "payment_callback_log_seq_tbl",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_callback_log_seq")
    @Column(name = "payment_callback_log_id")
    private Long id;

    // 결제 엔티티 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    // PG 콜백 이벤트 id
    @Column(name = "callback_event_id", nullable = false, updatable = false, length = 100)
    private String callbackEventId;

    // 결제 번호
    @Column(name = "payment_no", nullable = false, updatable = false, length = 50)
    private String paymentNo;

    // PG 콜백 결제 결과 상태
    @Column(name = "result_status", nullable = false, updatable = false, length = 30)
    private String resultStatus;

    // 콜백 결제 금액
    @Column(name = "amount", nullable = false, updatable = false)
    private int amount;

    // 원본 콜백 payload
    @Lob
    @Column(name = "raw_payload", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String rawPayload;

    // 콜백 처리 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 30)
    private CallbackProcessStatus processStatus;

    // 콜백 처리 메시지
    @Column(name = "message", length = 500)
    private String message;

    // 콜백 수신 시간
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    protected PaymentCallbackLog(
            Payment payment,
            String callbackEventId,
            String paymentNo,
            String resultStatus,
            int amount,
            String rawPayload,
            CallbackProcessStatus processStatus,
            String message,
            LocalDateTime receivedAt
    ) {
        this.payment = payment;
        this.callbackEventId = callbackEventId;
        this.paymentNo = paymentNo;
        this.resultStatus = resultStatus;
        this.amount = amount;
        this.rawPayload = rawPayload;
        this.processStatus = processStatus;
        this.message = message;
        this.receivedAt = receivedAt;
    }

    /**
     * 결제 콜백 로그 생성
     */
    public static PaymentCallbackLog create(
            Payment payment,
            String callbackEventId,
            String paymentNo,
            String resultStatus,
            int amount,
            String rawPayload,
            CallbackProcessStatus processStatus,
            String message,
            LocalDateTime receivedAt
    ) {
        return new PaymentCallbackLog(
                payment,
                callbackEventId,
                paymentNo,
                resultStatus,
                amount,
                rawPayload,
                processStatus,
                message,
                receivedAt
        );
    }
}
