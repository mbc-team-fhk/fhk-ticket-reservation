package com.fhk.ticketing.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhk.common.api.ApiWrapper;
import com.fhk.ticketing.payment.domain.Payment;
import com.fhk.ticketing.payment.domain.PaymentCallbackLog;
import com.fhk.ticketing.payment.dto.PaymentDtos.*;
import com.fhk.ticketing.payment.enums.CallbackProcessStatus;
import com.fhk.ticketing.payment.repository.PaymentCallbackLogRepository;
import com.fhk.ticketing.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 결제 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final ObjectMapper objectMapper;

    // reservation-service 호출 주소
    @Value("${client.reservation.url:http://localhost:9101}")
    private String reservationServiceUrl;

    /**
     * 결제 요청 생성
     */
    @Override
    @Transactional
    public PaymentRes createPayment(Long accountId, String authorization, CreatePaymentReq request) {

        // 1. 멱등키로 기존 결제 요청 조회
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(PaymentRes::from)
                .orElseGet(() -> createNewPayment(accountId, authorization, request));
    }

    /**
     * 결제 단건 조회
     */
    @Override
    public PaymentRes getPayment(String paymentNo) {
        return PaymentRes.from(getPaymentEntity(paymentNo));
    }

    /**
     * PG 콜백 처리
     */
    @Override
    @Transactional
    public CallbackResultRes processCallback(PgCallbackReq request) {

        // 1. paymentNo로 결제 조회 및 비관적 락 처리
        Payment payment = paymentRepository.findByPaymentNoForUpdate(request.paymentNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. paymentNo=" + request.paymentNo()));

        // 2. 이미 처리한 콜백 이벤트인지 확인
        if (callbackLogRepository.existsByCallbackEventId(request.callbackEventId())) {
            PaymentCallbackLog existing = callbackLogRepository.findByCallbackEventId(request.callbackEventId())
                    .orElseThrow();
            return new CallbackResultRes(
                    CallbackProcessStatus.DUPLICATED.name(),
                    PaymentRes.from(payment),
                    CallbackLogRes.from(existing)
            );
        }

        // 3. 콜백 금액과 결제 요청 금액 일치 여부 확인
        if (request.amount() != payment.getAmount()) {
            PaymentCallbackLog log = saveLog(
                    payment,
                    request,
                    CallbackProcessStatus.INVALID,
                    "콜백 금액이 결제 요청 금액과 일치하지 않습니다."
            );
            return new CallbackResultRes(log.getProcessStatus().name(), PaymentRes.from(payment), CallbackLogRes.from(log));
        }

        // 4. 콜백 상태에 따라 결제 승인/실패 처리
        if ("APPROVED".equals(request.status())) {
            payment.approve(request.approvedAt() == null ? LocalDateTime.now() : request.approvedAt());
            notifyReservation(payment, request);
        } else if ("FAILED".equals(request.status())) {
            payment.fail(LocalDateTime.now());
            notifyReservation(payment, request);
        } else {
            PaymentCallbackLog log = saveLog(
                    payment,
                    request,
                    CallbackProcessStatus.INVALID,
                    "지원하지 않는 콜백 상태입니다."
            );
            return new CallbackResultRes(log.getProcessStatus().name(), PaymentRes.from(payment), CallbackLogRes.from(log));
        }

        // 5. 정상 처리 콜백 로그 저장
        PaymentCallbackLog log = saveLog(payment, request, CallbackProcessStatus.PROCESSED, "콜백 처리가 완료되었습니다.");
        return new CallbackResultRes(log.getProcessStatus().name(), PaymentRes.from(payment), CallbackLogRes.from(log));
    }

    /**
     * Mock PG 결제 성공 콜백 실행
     */
    @Override
    public CallbackResultRes mockSuccess(String paymentNo) {

        Payment payment = getPaymentEntity(paymentNo);
        return processCallback(new PgCallbackReq(
                "PG-" + UUID.randomUUID(),
                paymentNo,
                "APPROVED",
                payment.getAmount(),
                LocalDateTime.now()
        ));
    }

    /**
     * Mock PG 결제 실패 콜백 실행
     */
    @Override
    public CallbackResultRes mockFail(String paymentNo) {

        Payment payment = getPaymentEntity(paymentNo);

        return processCallback(new PgCallbackReq(
                "PG-" + UUID.randomUUID(),
                paymentNo,
                "FAILED",
                payment.getAmount(),
                null
        ));
    }

    /**
     * 결제 콜백 로그 조회
     */
    @Override
    public CallbackLogListRes getCallbacks(String paymentNo) {

        // 1. paymentNo 기준 콜백 로그 목록 조회
        List<CallbackLogRes> logs = callbackLogRepository.findAllByPayment_PaymentNoOrderByReceivedAtAsc(paymentNo)
                .stream()
                .map(CallbackLogRes::from)
                .toList();
        return new CallbackLogListRes(logs);
    }

    /**
     * 신규 결제 요청 생성
     */
    private PaymentRes createNewPayment(Long accountId, String authorization, CreatePaymentReq request) {

        // 1. reservation-service에서 예약 정보 조회
        ApiWrapper<ReservationRes> response = RestClient.create(reservationServiceUrl)
                .get()
                .uri("/reservations/{reservationId}", request.reservationId())
                .header("Authorization", authorization)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        ReservationRes reservation = response == null ? null : response.getResult();

        // 2. 예약 조회 결과 확인
        if (reservation == null) {
            throw new IllegalStateException("예약 정보를 조회하지 못했습니다.");
        }
        if (!"PENDING_PAYMENT".equals(reservation.status())) {
            throw new IllegalStateException("결제 대기 상태의 예약만 결제를 요청할 수 있습니다.");
        }
        if (reservation.totalAmount() != request.amount()) {
            throw new IllegalArgumentException("결제 금액이 예약 금액과 일치하지 않습니다.");
        }

        // 3. 결제 요청 저장
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.request(
                nextPaymentNo(now),
                request.reservationId(),
                request.idempotencyKey(),
                request.amount(),
                now
        );
        return PaymentRes.from(paymentRepository.save(payment));
    }

    /**
     * 결제 엔티티 조회
     */
    private Payment getPaymentEntity(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. paymentNo=" + paymentNo));
    }

    /**
     * 예약 서비스 결제 결과 통지
     */
    private void notifyReservation(Payment payment, PgCallbackReq request) {

        // 1. reservation-service 내부 결제 결과 반영 API 호출
        RestClient.create(reservationServiceUrl)
                .post()
                .uri("/internal/reservations/{reservationId}/payment-result", payment.getReservationId())
                .body(new ReservationPaymentResultReq(
                        payment.getPaymentNo(),
                        request.status(),
                        request.amount(),
                        request.callbackEventId()
                ))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 결제 콜백 로그 저장
     */
    private PaymentCallbackLog saveLog(
            Payment payment,
            PgCallbackReq request,
            CallbackProcessStatus processStatus,
            String message
    ) {

        // 1. 콜백 요청 payload를 JSON 문자열로 저장
        PaymentCallbackLog log = PaymentCallbackLog.create(
                payment,
                request.callbackEventId(),
                request.paymentNo(),
                request.status(),
                request.amount(),
                toJson(request),
                processStatus,
                message,
                LocalDateTime.now()
        );
        return callbackLogRepository.save(log);
    }

    /**
     * 콜백 요청 JSON 변환
     */
    private String toJson(PgCallbackReq request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 결제 번호 생성
     */
    private String nextPaymentNo(LocalDateTime now) {
        return "PAY-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID();
    }
}
