package com.fhk.ticketing.reservation.demo;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 데모 상영 정보를 메모리에서 관리하는 카탈로그.
 */
@Component
public class DemoScreeningCatalog {

    private final List<DemoScreening> screenings;

    /**
     * 테스트용 상영 목록 주입
     */
    public DemoScreeningCatalog() {
        this.screenings = List.of(
                new DemoScreening(
                        1L,
                        "인터스텔라 2D",
                        "FHK 시네마 병점점",
                        "1관",
                        LocalDate.now().atTime(LocalTime.of(19, 30)),
                        createSeats(1000L, 14_000)
                ),
                new DemoScreening(
                        2L,
                        "프로젝트 헤일메리",
                        "FHK 시네마 병점점",
                        "2관",
                        LocalDate.now().atTime(LocalTime.of(21, 10)),
                        createSeats(2000L, 15_000)
                )
        );
    }

    /**
     * 전체 데모 상영 목록 조회
     */
    public List<DemoScreening> findAll() {
        return screenings;
    }

    /**
     * screeningId 기준 데모 상영 정보 조회
     */
    public DemoScreening getScreening(Long screeningId) {
        return screenings.stream()
                .filter(screening -> screening.screeningId().equals(screeningId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown screeningId: " + screeningId));
    }

    /**
     * 상영 id와 좌석 id 기준 데모 좌석 정보 조회
     */
    public DemoSeat getSeat(Long screeningId, Long seatId) {
        return getScreening(screeningId).seats().stream()
                .filter(seat -> seat.seatId().equals(seatId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown seatId: " + seatId));
    }

    /**
     * 데모 상영 좌석 목록 생성
     */
    private static List<DemoSeat> createSeats(Long baseSeatId, int price) {
        List<DemoSeat> seats = new ArrayList<>();

        // 1. 데모 좌석 행 정보를 준비한다.
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};

        // 2. 행과 좌석 번호를 기준으로 seatId, label을 만든다.
        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            for (int number = 1; number <= 12; number++) {
                long seatId = baseSeatId + (rowIndex * 12L) + number;
                String label = rows[rowIndex] + number;
                seats.add(new DemoSeat(seatId, rows[rowIndex], number, label, price));
            }
        }
        return seats;
    }
}
