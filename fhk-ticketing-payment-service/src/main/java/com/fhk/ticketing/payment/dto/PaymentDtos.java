package com.fhk.ticketing.payment.dto;

import com.fhk.ticketing.payment.domain.Payment;
import com.fhk.ticketing.payment.domain.PaymentCallbackLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 API 요청/응답 DTO 모음
 */
public class PaymentDtos {

    /**
     * 결제 생성 요청
     */
    public record CreatePaymentReq(
            @NotNull Long reservationId,
            @Positive int amount,
            @NotBlank String idempotencyKey
    ) {
    }

    /**
     * 결제 응답
     */
    public record PaymentRes(
            Long paymentId,
            String paymentNo,
            Long reservationId,
            int amount,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            LocalDateTime failedAt
    ) {
        /**
         * 결제 응답 변환
         */
        public static PaymentRes from(Payment payment) {

            // 1. 결제 엔티티 값을 응답 필드로 매핑
            return new PaymentRes(
                    payment.getId(),
                    payment.getPaymentNo(),
                    payment.getReservationId(),
                    payment.getAmount(),
                    payment.getStatus().name(),
                    payment.getRequestedAt(),
                    payment.getApprovedAt(),
                    payment.getFailedAt()
            );
        }
    }

    /**
     * PG 콜백 요청
     */
    public record PgCallbackReq(
            @NotBlank String callbackEventId,
            @NotBlank String paymentNo,
            @NotBlank String status,
            @Positive int amount,
            LocalDateTime approvedAt
    ) {
    }

    /**
     * 결제 콜백 로그 응답
     */
    public record CallbackLogRes(
            Long callbackLogId,
            String callbackEventId,
            String paymentNo,
            String resultStatus,
            int amount,
            String processStatus,
            String message,
            LocalDateTime receivedAt
    ) {
        /**
         * 결제 콜백 로그 응답 변환
         */
        public static CallbackLogRes from(PaymentCallbackLog log) {

            // 1. 콜백 로그 엔티티 값을 응답 필드로 매핑
            return new CallbackLogRes(
                    log.getId(),
                    log.getCallbackEventId(),
                    log.getPaymentNo(),
                    log.getResultStatus(),
                    log.getAmount(),
                    log.getProcessStatus().name(),
                    log.getMessage(),
                    log.getReceivedAt()
            );
        }
    }

    /**
     * PG 콜백 처리 결과 응답
     */
    public record CallbackResultRes(
            String processStatus,
            PaymentRes payment,
            CallbackLogRes callbackLog
    ) {
    }

    /**
     * 예약 서비스 예약 응답
     */
    public record ReservationRes(
            Long reservationId,
            String reservationNo,
            Long screeningId,
            String status,
            int totalAmount
    ) {
    }

    /**
     * 예약 서비스 결제 결과 반영 요청
     */
    public record ReservationPaymentResultReq(
            String paymentNo,
            String resultStatus,
            int amount,
            String callbackEventId
    ) {
    }

    /**
     * 결제 콜백 로그 목록 응답
     */
    public record CallbackLogListRes(
            List<CallbackLogRes> callbacks
    ) {
    }
}
