package com.fhk.ticketing.reservation.controller;

import com.fhk.common.api.ApiResponse;
import com.fhk.security.core.record.FhkUserPrincipal;
import com.fhk.ticketing.reservation.dto.ReservationDtos.CreateReservationReq;
import com.fhk.ticketing.reservation.dto.ReservationDtos.PaymentResultReq;
import com.fhk.ticketing.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 데모 상영 목록 조회
     * @return
     */
    @GetMapping("/demo/screenings")
    public ResponseEntity<?> getDemoScreenings() {
        return ApiResponse.ok(reservationService.getDemoScreenings());
    }

    /**
     * 데모 상영 상세 정보 조회
     */
    @GetMapping("/demo/screenings/{screeningId}")
    public ResponseEntity<?> getDemoScreening(@PathVariable Long screeningId) {
        return ApiResponse.ok(reservationService.getDemoScreening(screeningId));
    }

    /**
     * 상영별 좌석 점유 상태 조회
     */
    @GetMapping("/screenings/{screeningId}/seats")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long screeningId) {
        return ApiResponse.ok(reservationService.getSeatStatus(screeningId));
    }

    /**
     * 선택 좌석 기준 결제 대기 예약 생성
     */
    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(
            @AuthenticationPrincipal FhkUserPrincipal principal,
            @Valid @RequestBody CreateReservationReq request
    ) {
        return ApiResponse.created(reservationService.createReservation(principal.id(), request));
    }

    /**
     * 로그인 사용자 예약 상세 정보 조회
     */
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> getReservation(
            @AuthenticationPrincipal FhkUserPrincipal principal,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.ok(reservationService.getReservation(principal.id(), reservationId));
    }

    /**
     * 결제 대기 시간 초과 예약 만료 처리
     */
    @PostMapping("/reservations/expire")
    public ResponseEntity<?> expirePendingReservations() {
        return ApiResponse.ok(Map.of("expiredCount", reservationService.expirePendingReservations()));
    }

    /**
     * 결제 서비스 결제 결과 예약 반영
     */
    @PostMapping("/internal/reservations/{reservationId}/payment-result")
    public ResponseEntity<?> applyPaymentResult(
            @PathVariable Long reservationId,
            @RequestBody PaymentResultReq request
    ) {
        return ApiResponse.ok(reservationService.applyPaymentResult(reservationId, request));
    }

    /**
     * 데모 예약 데이터 초기화
     */
    @PostMapping("/demo/reset")
    public ResponseEntity<?> resetDemoData() {
        reservationService.resetDemoData();
        return ApiResponse.ok(Map.of("status", "RESET"));
    }
}
