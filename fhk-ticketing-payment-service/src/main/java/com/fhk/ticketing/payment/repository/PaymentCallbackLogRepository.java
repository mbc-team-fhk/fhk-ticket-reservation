package com.fhk.ticketing.payment.repository;

import com.fhk.ticketing.payment.domain.PaymentCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 결제 콜백 로그 Repository
 */
public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {

    /**
     * 콜백 이벤트 id 중복 여부 확인
     */
    boolean existsByCallbackEventId(String callbackEventId);

    /**
     * 콜백 이벤트 id 기준 로그 조회
     */
    Optional<PaymentCallbackLog> findByCallbackEventId(String callbackEventId);

    /**
     * 결제 번호 기준 콜백 로그 목록 조회
     */
    List<PaymentCallbackLog> findAllByPayment_PaymentNoOrderByReceivedAtAsc(String paymentNo);
}
