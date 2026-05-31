package com.fhk.ticketing.payment.controller;

import com.fhk.common.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RestControllerAdvice
public class PaymentApiExceptionHandler {

    /**
     * 잘못된 요청값 예외 400 응답 처리
     * TODO : GlobalException Handler로 빼기
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 현재 결제 상태와 맞지 않는 요청 409 응답 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException e) {
        return ApiResponse.error(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    /**
     * 예약 서비스 연동 실패 502 응답 처리
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<?> handleRestClient(RestClientResponseException e) {
        return ApiResponse.error(
                HttpStatus.BAD_GATEWAY.value(),
                "reservation-service error: " + e.getResponseBodyAsString()
        );
    }

    /**
     * DB 락 경합은 비즈니스 충돌이 아니라 재시도 가능한 일시 장애로 처리
     */
    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<?> handleLockConflict(CannotAcquireLockException e) {
        log.warn("[payment-api] lock conflict while processing payment", e);

        return ApiResponse.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "일시적인 DB 경합으로 결제 요청 처리에 실패했습니다. 잠시 후 다시 시도해주세요."
        );
    }

    /**
     * DB 제약 충돌은 원인을 분리해서 응답 처리
     */
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<?> handleDataConflict(Exception e) {
        if (containsMessage(e, "uk_payment_idempotency_key")) {
            log.warn("[payment-api] idempotency key conflict", e);

            return ApiResponse.error(
                    HttpStatus.CONFLICT.value(),
                    "이미 처리 중인 결제 요청입니다."
            );
        }

        log.error("[payment-api] data integrity violation", e);

        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "결제 데이터 처리 중 오류가 발생했습니다."
        );
    }

    private boolean containsMessage(Throwable e, String text) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(text)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
