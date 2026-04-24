package com.banking.transfer.exception;

import com.banking.common.exception.BankingException;

/**
 * 같은 X-Idempotency-Key 로 요청했지만 저장된 페이로드의 스냅샷을 역직렬화할 수 없거나
 * 현재 요청과 모순될 때. (토이 수준에서는 역직렬 실패만 다룸)
 */
public class IdempotencyConflictException extends BankingException {

    public IdempotencyConflictException(String key, String reason) {
        super("IDEMPOTENCY_CONFLICT",
                "멱등성 키 처리 중 충돌이 발생했습니다 (key: %s, reason: %s)".formatted(key, reason));
    }
}
