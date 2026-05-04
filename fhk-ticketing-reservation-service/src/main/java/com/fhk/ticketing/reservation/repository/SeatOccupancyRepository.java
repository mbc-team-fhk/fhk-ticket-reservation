package com.fhk.ticketing.reservation.repository;

import com.fhk.ticketing.reservation.domain.SeatOccupancy;
import com.fhk.ticketing.reservation.enums.SeatOccupancyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 좌석 점유 Repository
 */
public interface SeatOccupancyRepository extends JpaRepository<SeatOccupancy, Long> {

    /**
     * 특정 상영 회차의 특정 좌석 점유 여부 확인
     */
    boolean existsByScreeningIdAndSeatId(
            Long screeningId,
            Long seatId
    );

    /**
     * 여러 좌석 중 이미 점유된 좌석 조회
     * - 예약 생성 전에 선점 여부 확인용
     */
    List<SeatOccupancy> findAllByScreeningIdAndSeatIdIn(
            Long screeningId,
            Collection<Long> seatIds
    );

    /**
     * 예약 ID 기준 점유 좌석 조회
     */
    List<SeatOccupancy> findAllByReservation_Id(
            Long reservationId
    );

    /**
     * 예약 ID 기준 점유 좌석 조회 + 락
     * - 결제 성공 / 실패 / 만료 / 취소 처리 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select so
            from SeatOccupancy so
            where so.reservation.id = :reservationId
            order by so.seatId asc
            """)
    List<SeatOccupancy> findAllByReservationIdForUpdate(
            @Param("reservationId") Long reservationId
    );

    /**
     * 특정 좌석 점유 조회 + 락
     * - 필요 시 단건 상태 변경용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select so
            from SeatOccupancy so
            where so.screeningId = :screeningId
              and so.seatId = :seatId
            """)
    Optional<SeatOccupancy> findByScreeningIdAndSeatIdForUpdate(
            @Param("screeningId") Long screeningId,
            @Param("seatId") Long seatId
    );

    /**
     * 만료된 HELD 좌석 점유 조회
     * - 스케줄러에서 사용
     */
    List<SeatOccupancy> findAllByStatusAndExpiresAtBefore(
            SeatOccupancyStatus status,
            LocalDateTime now
    );

    /**
     * 만료된 HELD 좌석 점유 조회 + 락
     * - 여러 pod에서 만료 스케줄러가 동시에 돌 가능성이 있으면 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select so
            from SeatOccupancy so
            where so.status = :status
              and so.expiresAt < :now
            order by so.expiresAt asc
            """)
    List<SeatOccupancy> findExpiredHeldOccupanciesForUpdate(
            @Param("status") SeatOccupancyStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * 결제 실패 / 만료 / 취소 시 현재 점유 삭제
     */
    void deleteAllByReservation_Id(
            Long reservationId
    );
}
