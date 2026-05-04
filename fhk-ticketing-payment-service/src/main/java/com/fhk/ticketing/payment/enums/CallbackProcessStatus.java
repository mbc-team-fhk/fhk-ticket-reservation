package com.fhk.ticketing.payment.enums;

/**
 * PG 콜백 처리 상태
 */
public enum CallbackProcessStatus {
    PROCESSED,   // 정상 처리
    DUPLICATED,  // 중복 콜백
    INVALID      // 유효하지 않은 콜백
}
