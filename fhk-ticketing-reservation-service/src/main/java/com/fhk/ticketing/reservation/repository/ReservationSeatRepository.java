package com.fhk.ticketing.reservation.repository;

import com.fhk.ticketing.reservation.domain.ReservationSeat;
import com.fhk.ticketing.reservation.enums.ReservationSeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 예약 좌석 Repository
 */
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    /**
     * 예약 ID 기준 좌석 목록 조회
     * - 예매 상세 조회에서 사용
     */
    List<ReservationSeat> findAllByReservationIdOrderBySeatRowAscSeatNumberAsc(
            Long reservationId
    );

    /**
     * 여러 예약의 좌석 목록 조회
     * - 내 예매 목록 조회 후 좌석 정보 매핑할 때 사용
     */
    List<ReservationSeat> findAllByReservationIdInOrderByReservationIdAscSeatRowAscSeatNumberAsc(
            Collection<Long> reservationIds
    );

    /**
     * 예약 ID + 좌석 ID 기준 조회
     */
    Optional<ReservationSeat> findByReservationIdAndSeatId(
            Long reservationId,
            Long seatId
    );

    /**
     * 예약 내 같은 좌석 중복 방지 확인용
     */
    boolean existsByReservationIdAndSeatId(
            Long reservationId,
            Long seatId
    );

    /**
     * 특정 상영 회차의 예매 좌석 이력 조회
     * - 관리자/디버깅용
     */
    List<ReservationSeat> findAllByScreeningIdOrderBySeatRowAscSeatNumberAsc(
            Long screeningId
    );

    /**
     * 특정 상영 회차의 특정 좌석 예매 이력 조회
     * - 디버깅용
     */
    List<ReservationSeat> findAllByScreeningIdAndSeatIdOrderByIdDesc(
            Long screeningId,
            Long seatId
    );

    /**
     * 상태 기준 조회
     * - 만료/취소/확정 처리 검증용
     */
    List<ReservationSeat> findAllByReservationIdAndStatus(
            Long reservationId,
            ReservationSeatStatus status
    );

    /**
     * 예약 좌석 상태 변경용 락 조회
     * - 결제 성공 / 결제 실패 / 만료 / 취소 처리 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select rs
            from ReservationSeat rs
            where rs.reservation.id = :reservationId
            order by rs.seatRow asc, rs.seatNumber asc
            """)
    List<ReservationSeat> findAllByReservationIdForUpdate(
            Long reservationId
    );

    /**
     * 특정 예약의 특정 좌석 상태 변경용 락 조회
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select rs
            from ReservationSeat rs
            where rs.reservation.id = :reservationId
              and rs.seatId = :seatId
            """)
    Optional<ReservationSeat> findByReservationIdAndSeatIdForUpdate(
            Long reservationId,
            Long seatId
    );
}
