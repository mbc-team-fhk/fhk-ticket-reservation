package com.fhk.ticketing.reservation.repository;

import com.fhk.ticketing.reservation.domain.Reservation;
import com.fhk.ticketing.reservation.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 예약 Repository
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 예매번호 중복 여부 확인
     */
    boolean existsByReservationNo(String reservationNo);

    /**
     * 예매번호 기준 예약 조회
     */
    Optional<Reservation> findByReservationNo(String reservationNo);

    /**
     * 본인 예약 단건 조회
     */
    Optional<Reservation> findByIdAndAccountId(Long id, Long accountId);

    /**
     * 예매번호와 accountId 기준 본인 예약 조회
     */
    Optional<Reservation> findByReservationNoAndAccountId(String reservationNo, Long accountId);

    /**
     * 내 예약 목록 조회
     */
    Page<Reservation> findAllByAccountIdOrderByRegTimeDesc(
            Long accountId,
            Pageable pageable
    );

    /**
     * 특정 상영 회차 예약 목록 조회
     * - 관리자/내부 검증용
     */
    Page<Reservation> findAllByScreeningIdOrderByRegTimeDesc(
            Long screeningId,
            Pageable pageable
    );

    /**
     * 결제 대기 만료 대상 조회
     * - 스케줄러에서 사용
     */
    List<Reservation> findAllByStatusAndExpiresAtLessThanEqual(
            ReservationStatus status,
            LocalDateTime now
    );

    /**
     * 결제 성공 / 취소 / 만료 처리 시 동시 상태 변경 방지용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(Long id);

    /**
     * 예매번호 기준 상태 변경 락
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.reservationNo = :reservationNo")
    Optional<Reservation> findByReservationNoForUpdate(String reservationNo);

    /**
     * 본인 예약 취소 시 사용 가능
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from Reservation r
            where r.id = :id
              and r.accountId = :accountId
            """)
    Optional<Reservation> findByIdAndAccountIdForUpdate(
            Long id,
            Long accountId
    );
}
