package com.fhk.ticketing.payment.controller;

import com.fhk.common.api.ApiResponse;
import com.fhk.security.core.record.FhkUserPrincipal;
import com.fhk.ticketing.payment.dto.PaymentDtos.CreatePaymentReq;
import com.fhk.ticketing.payment.dto.PaymentDtos.PgCallbackReq;
import com.fhk.ticketing.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청 생성
     */
    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(
            @AuthenticationPrincipal FhkUserPrincipal principal,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreatePaymentReq request
    ) {
        return ApiResponse.created(paymentService.createPayment(principal.id(), authorization, request));
    }

    /**
     * 결제 단건 조회
     */
    @GetMapping("/payments/{paymentNo}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.getPayment(paymentNo));
    }

    /**
     * Mock PG 결제 성공 콜백 실행
     */
    @PostMapping("/mock-pg/payments/{paymentNo}/success")
    public ResponseEntity<?> mockSuccess(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.mockSuccess(paymentNo));
    }

    /**
     * Mock PG 결제 실패 콜백 실행
     */
    @PostMapping("/mock-pg/payments/{paymentNo}/fail")
    public ResponseEntity<?> mockFail(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.mockFail(paymentNo));
    }

    /**
     * PG 콜백 처리
     */
    @PostMapping("/pg/callback")
    public ResponseEntity<?> processCallback(@Valid @RequestBody PgCallbackReq request) {
        return ApiResponse.ok(paymentService.processCallback(request));
    }

    /**
     * 결제 콜백 로그 조회
     */
    @GetMapping("/payments/{paymentNo}/callbacks")
    public ResponseEntity<?> getCallbacks(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.getCallbacks(paymentNo));
    }
}
