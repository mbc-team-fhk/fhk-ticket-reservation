package com.fhk.ticketing.payment.controller;

import com.fhk.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

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
}
