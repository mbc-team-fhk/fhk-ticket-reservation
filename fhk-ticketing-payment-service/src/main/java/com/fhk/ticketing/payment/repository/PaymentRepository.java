package com.fhk.ticketing.payment.repository;

import com.fhk.ticketing.payment.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * 결제 Repository
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 결제 번호 기준 결제 조회
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * 멱등키 기준 결제 조회
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * 결제 번호 기준 결제 조회 + 락
     * - PG 콜백 처리 시 중복 상태 변경 방지
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentNo = :paymentNo")
    Optional<Payment> findByPaymentNoForUpdate(String paymentNo);
}
