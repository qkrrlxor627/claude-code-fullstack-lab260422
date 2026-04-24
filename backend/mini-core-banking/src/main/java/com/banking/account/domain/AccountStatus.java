package com.banking.account.domain;

/**
 * 계좌 상태.
 * 상태 전이 규칙:
 *   ACTIVE  → DORMANT (6개월 이상 거래 없음, Phase 5 에서 배치 도입 예정)
 *   ACTIVE  → CLOSED  (해지, 잔액 0 필수)
 *   DORMANT → ACTIVE  (거래 재개)
 *   DORMANT → CLOSED  (해지)
 * CLOSED 는 종단 상태 (어떤 전이도 불가).
 */
public enum AccountStatus {
    ACTIVE,
    DORMANT,
    CLOSED
}
