# fhk-ticket-reservation

좌석 예매와 결제 흐름을 검증하기 위한 ticketing 도메인 repo입니다. 하나의 Gradle 루트 아래에 reservation service와 payment service가 분리되어 있습니다.

## Services

| Module | Port | Role |
| --- | --- | --- |
| `fhk-ticketing-reservation-service` | `9101` | 상영/좌석 조회, 좌석 점유, 예약 생성, 결제 결과 반영 |
| `fhk-ticketing-payment-service` | `9102` | 결제 요청 생성, Mock PG callback, 결제 이력 조회 |

## Main Flow

```text
좌석 조회
  -> 예약 생성
  -> 좌석 HELD
  -> 결제 요청 생성
  -> PG callback
  -> 예약 RESERVED 또는 PAYMENT_FAILED
```

reservation service는 결제 대기 예약을 만들고 선택 좌석을 일정 시간 동안 점유합니다. payment service는 예약 상태와 금액을 확인한 뒤 결제 요청을 만들고, Mock PG callback 결과를 reservation service에 다시 전달합니다.

## Consistency Points

- 동일 상영/좌석 중복 점유 방지: `screening_id + seat_id` unique constraint
- 예약 번호 충돌 방지: `RSV-yyyyMMdd-UUID`
- 결제 번호 충돌 방지: `PAY-yyyyMMdd-UUID`
- 결제 중복 요청 방지: `idempotency_key` unique constraint
- callback 중복 처리 방지: `callback_event_id` unique constraint
- 결제 성공/실패 callback 이후 reservation service에 결과 반영
- 만료된 결제 대기 예약은 스케줄러와 조회 시점 정리 로직으로 회수

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Security
- JPA, MariaDB
- Redis
- H2 test profile
- Docker, k3s

## Run Locally

Reservation service:

```bash
./gradlew :fhk-ticketing-reservation-service:bootRun
```

Payment service:

```bash
./gradlew :fhk-ticketing-payment-service:bootRun
```

Windows에서는 `./gradlew` 대신 `gradlew.bat`을 사용할 수 있습니다.

대표 환경 변수입니다.

```env
DB_URL=jdbc:mariadb://localhost:3306/fhk_ticketing_reservation_db
DB_USER=root
DB_PW=
REDIS_HOST=localhost
REDIS_PORT=6379
RESERVATION_SERVICE_URL=http://localhost:9101
```
