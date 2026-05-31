package com.fhk.ticketing.reservation.service;

import com.fhk.ticketing.reservation.dto.DemoDtos.ScreeningDetailRes;
import com.fhk.ticketing.reservation.dto.DemoDtos.ScreeningSummaryRes;
import com.fhk.ticketing.reservation.dto.ReservationDtos.CreateReservationReq;
import com.fhk.ticketing.reservation.dto.ReservationDtos.PaymentResultReq;
import com.fhk.ticketing.reservation.dto.ReservationDtos.ReservationRes;
import com.fhk.ticketing.reservation.dto.ReservationDtos.SeatStatusRes;

import java.util.List;

public interface ReservationService {

    List<ScreeningSummaryRes> getDemoScreenings();

    ScreeningDetailRes getDemoScreening(Long screeningId);

    SeatStatusRes getSeatStatus(Long screeningId);

    ReservationRes createReservation(Long accountId, CreateReservationReq request);

    ReservationRes getReservation(Long accountId, Long reservationId);

    ReservationRes applyPaymentResult(Long reservationId, PaymentResultReq request);

    int expirePendingReservations();

    void resetDemoData();
}
