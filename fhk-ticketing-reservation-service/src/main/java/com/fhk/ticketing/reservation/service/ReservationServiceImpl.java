package com.fhk.ticketing.reservation.service;

import com.fhk.ticketing.reservation.demo.DemoScreeningCatalog;
import com.fhk.ticketing.reservation.demo.DemoSeat;
import com.fhk.ticketing.reservation.domain.Reservation;
import com.fhk.ticketing.reservation.domain.ReservationSeat;
import com.fhk.ticketing.reservation.domain.SeatOccupancy;
import com.fhk.ticketing.reservation.dto.DemoDtos.ScreeningDetailRes;
import com.fhk.ticketing.reservation.dto.DemoDtos.ScreeningSummaryRes;
import com.fhk.ticketing.reservation.dto.ReservationDtos.*;
import com.fhk.ticketing.reservation.enums.ReservationStatus;
import com.fhk.ticketing.reservation.repository.ReservationRepository;
import com.fhk.ticketing.reservation.repository.ReservationSeatRepository;
import com.fhk.ticketing.reservation.repository.SeatOccupancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 예약 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    // 좌석 선점 유지 시간
    private static final int HOLD_MINUTES = 5;

    private final DemoScreeningCatalog demoScreeningCatalog;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final SeatOccupancyRepository seatOccupancyRepository;

    /**
     * 데모 상영 목록 조회
     */
    @Override
    public List<ScreeningSummaryRes> getDemoScreenings() {
        return demoScreeningCatalog.findAll().stream()
                .map(ScreeningSummaryRes::from)
                .toList();
    }

    /**
     * 데모 상영 상세 정보 조회
     */
    @Override
    public ScreeningDetailRes getDemoScreening(Long screeningId) {
        return ScreeningDetailRes.from(demoScreeningCatalog.getScreening(screeningId));
    }

    /**
     * 상영별 좌석 점유 상태 조회
     */
    @Override
    public SeatStatusRes getSeatStatus(Long screeningId) {

        // 1. 조회 전에 만료된 결제 대기 예약 정리
        expirePendingReservations();

        // 2. 현재 상영의 좌석 점유 정보를 seatId 기준으로 매핑
        Map<Long, SeatOccupancy> occupancyBySeatId = seatOccupancyRepository
                .findAllByScreeningIdAndSeatIdIn(
                        screeningId,
                        demoScreeningCatalog.getScreening(screeningId).seats().stream()
                                .map(DemoSeat::seatId)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(SeatOccupancy::getSeatId, occupancy -> occupancy));

        // 3. 데모 좌석 목록에 점유 상태를 합쳐 응답 생성
        List<SeatStatusItem> seats = demoScreeningCatalog.getScreening(screeningId).seats().stream()
                .map(seat -> {
                    SeatOccupancy occupancy = occupancyBySeatId.get(seat.seatId());
                    String status = occupancy == null ? "AVAILABLE" : occupancy.getStatus().name();
                    return new SeatStatusItem(
                            seat.seatId(),
                            seat.seatRow(),
                            seat.seatNumber(),
                            seat.seatLabel(),
                            seat.price(),
                            status,
                            occupancy == null ? null : occupancy.getReservation().getId(),
                            occupancy == null ? null : occupancy.getExpiresAt()
                    );
                })
                .toList();

        return new SeatStatusRes(screeningId, seats);
    }

    /**
     * 결제 대기 예약 생성
     */
    @Override
    @Transactional
    public ReservationRes createReservation(Long accountId, CreateReservationReq request) {

        // 1. 만료된 결제 대기 예약 먼저 정리
        expirePendingReservations();

        // 2. 요청 좌석 중복 여부 확인
        List<Long> seatIds = new LinkedHashSet<>(request.seatIds()).stream().toList();
        if (seatIds.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("중복된 좌석이 포함되어 있습니다.");
        }

        // 3. 좌석 선점 만료 시간과 총 결제 금액 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);
        int totalAmount = seatIds.stream()
                .map(seatId -> demoScreeningCatalog.getSeat(request.screeningId(), seatId))
                .mapToInt(DemoSeat::price)
                .sum();

        // 4. 예약 헤더 저장
        Reservation reservation = reservationRepository.save(
                Reservation.createPending(
                        nextReservationNo(now),
                        accountId,
                        request.screeningId(),
                        totalAmount,
                        expiresAt
                )
        );

        // 5. 예약 좌석 이력 생성
        List<ReservationSeat> reservationSeats = seatIds.stream()
                .map(seatId -> {
                    DemoSeat seat = demoScreeningCatalog.getSeat(request.screeningId(), seatId);
                    return ReservationSeat.hold(
                            reservation,
                            request.screeningId(),
                            seat.seatId(),
                            seat.seatRow(),
                            seat.seatNumber(),
                            seat.seatLabel(),
                            seat.price()
                    );
                })
                .toList();

        // 6. 현재 좌석 점유 정보 생성
        List<SeatOccupancy> occupancies = seatIds.stream()
                .map(seatId -> SeatOccupancy.hold(reservation, request.screeningId(), seatId, expiresAt))
                .toList();

        // 7. 좌석 이력과 점유 정보를 저장하고 중복 선점 예외 처리
        try {
            reservationSeatRepository.saveAll(reservationSeats);
            seatOccupancyRepository.saveAll(occupancies);
            seatOccupancyRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("선택한 좌석 중 이미 선점되었거나 예매된 좌석이 있습니다.");
        }

        return ReservationRes.from(reservation, reservationSeats);
    }

    /**
     * 본인 예약 상세 조회
     */
    @Override
    public ReservationRes getReservation(Long accountId, Long reservationId) {

        // 1. reservationId로 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));

        // 2. 로그인 사용자 본인 예약인지 확인
        if (!reservation.getAccountId().equals(accountId)) {
            throw new IllegalStateException("해당 계정의 예약이 아닙니다.");
        }
        return toReservationRes(reservation);
    }

    /**
     * 내부용 예약 상세 조회
     */
    private ReservationRes getReservationInternal(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));
        return toReservationRes(reservation);
    }

    /**
     * 예약 응답 변환
     */
    private ReservationRes toReservationRes(Reservation reservation) {
        return ReservationRes.from(
                reservation,
                reservationSeatRepository.findAllByReservationIdOrderBySeatRowAscSeatNumberAsc(reservation.getId())
        );
    }

    /**
     * 결제 결과 예약 반영
     */
    @Override
    @Transactional
    public ReservationRes applyPaymentResult(Long reservationId, PaymentResultReq request) {

        // 1. reservationId로 예약 조회 및 비관적 락 처리
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));

        // 2. 결제 금액이 다르면 결제 실패로 처리
        if (request.amount() != reservation.getTotalAmount()) {
            reservation.failPayment(LocalDateTime.now());
            expireOrCancelSeats(reservation.getId(), false);
            return getReservationInternal(reservationId);
        }

        // 3. 결제 결과 상태에 따라 예약 확정 또는 실패 처리
        if ("APPROVED".equals(request.resultStatus())) {
            confirmReservation(reservation);
        } else if ("FAILED".equals(request.resultStatus())) {
            reservation.failPayment(LocalDateTime.now());
            expireOrCancelSeats(reservation.getId(), false);
        } else {
            throw new IllegalArgumentException("지원하지 않는 결제 결과 상태입니다. resultStatus=" + request.resultStatus());
        }

        return getReservationInternal(reservationId);
    }

    /**
     * 결제 대기 예약 만료 처리
     */
    @Override
    @Transactional
    public int expirePendingReservations() {

        // 1. 결제 대기 시간이 지난 예약 목록 조회
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationRepository.findAllByStatusAndExpiresAtLessThanEqual(
                ReservationStatus.PENDING_PAYMENT,
                now
        );

        // 2. 예약과 좌석 상태를 만료 처리
        reservations.forEach(reservation -> {
            reservation.expire(now);
            expireOrCancelSeats(reservation.getId(), true);
        });
        return reservations.size();
    }

    /**
     * 결제 대기 예약 만료 스케줄 실행
     */
    @Scheduled(fixedDelayString = "${ticketing.reservation.expire-fixed-delay-ms:15000}")
    @Transactional
    public void expirePendingReservationsOnSchedule() {
        expirePendingReservations();
    }

    /**
     * 데모 예약 데이터 초기화
     */
    @Override
    @Transactional
    public void resetDemoData() {

        // 1. FK 순서에 맞춰 좌석 점유 정보부터 삭제
        seatOccupancyRepository.deleteAll();

        // 2. 예약 좌석 이력 삭제
        reservationSeatRepository.deleteAll();

        // 3. 예약 헤더 삭제
        reservationRepository.deleteAll();
    }

    /**
     * 예약 및 좌석 확정 처리
     */
    private void confirmReservation(Reservation reservation) {

        // 1. 예약 헤더 확정 처리
        LocalDateTime now = LocalDateTime.now();
        reservation.confirm(now);

        // 2. 예약 좌석 이력 확정 처리
        reservationSeatRepository.findAllByReservationIdForUpdate(reservation.getId())
                .forEach(ReservationSeat::confirm);

        // 3. 좌석 점유 정보 확정 처리
        seatOccupancyRepository.findAllByReservationIdForUpdate(reservation.getId())
                .forEach(occupancy -> occupancy.confirm(now));
    }

    /**
     * 예약 좌석 만료 또는 취소 처리
     */
    private void expireOrCancelSeats(Long reservationId, boolean expired) {

        // 1. 예약 좌석 이력 상태 변경
        reservationSeatRepository.findAllByReservationIdForUpdate(reservationId)
                .forEach(seat -> {
                    if (expired) {
                        seat.expire();
                    } else {
                        seat.cancel();
                    }
                });

        // 2. 현재 좌석 점유 정보 삭제
        seatOccupancyRepository.deleteAllByReservation_Id(reservationId);
    }

    /**
     * 예약 번호 생성
     */
    private String nextReservationNo(LocalDateTime now) {
        return "RSV-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
