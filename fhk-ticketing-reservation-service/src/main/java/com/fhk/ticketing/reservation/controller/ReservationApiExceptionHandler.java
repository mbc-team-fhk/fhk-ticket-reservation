package com.fhk.ticketing.reservation.controller;

import com.fhk.common.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ReservationApiExceptionHandler {

    /**
     * 잘못된 요청값 예외 400 응답 처리
     * TODO : GlobalException Handler로 빼기
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 현재 예약 상태와 맞지 않는 요청 409 응답 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException e) {
        return ApiResponse.error(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    /**
     * DB 락 경합은 비즈니스 충돌이 아니라 재시도 가능한 일시 장애로 처리
     */
    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<?> handleLockConflict(CannotAcquireLockException e) {
        log.warn("[reservation-api] lock conflict while creating reservation", e);

        return ApiResponse.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "일시적인 DB 경합으로 예약 처리에 실패했습니다. 잠시 후 다시 시도해주세요."
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
        if (containsMessage(e, "uk_seat_occupancy_screening_seat")) {
            log.warn("[reservation-api] seat occupancy conflict", e);

            return ApiResponse.error(
                    HttpStatus.CONFLICT.value(),
                    "선택한 좌석 중 이미 선점되었거나 예매된 좌석이 있습니다."
            );
        }

        log.error("[reservation-api] data integrity violation", e);

        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "예약 데이터 처리 중 오류가 발생했습니다."
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
