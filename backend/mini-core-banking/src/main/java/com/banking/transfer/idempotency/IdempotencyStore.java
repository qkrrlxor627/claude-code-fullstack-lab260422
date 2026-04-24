package com.banking.transfer.idempotency;

import java.time.Duration;
import java.util.Optional;

/**
 * 멱등성 키 저장소 포트.
 * 이체 같은 부작용 있는 연산이 같은 키로 두 번 호출될 때
 * 실제 실행을 한 번만 보장하기 위한 캐시.
 */
public interface IdempotencyStore {

    /** 키에 대응하는 저장된 응답 JSON 이 있으면 반환. */
    Optional<String> find(String key);

    /**
     * SET NX EX 시맨틱. 이전에 같은 키가 없을 때만 저장되고 true 반환.
     * 이미 있으면 false.
     */
    boolean putIfAbsent(String key, String payload, Duration ttl);
}
