package com.banking.transfer.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * StringRedisTemplate 기반 IdempotencyStore.
 * {@code SET key value NX EX ttl} 로 원자적 putIfAbsent 구현.
 */
@Component
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;

    public RedisIdempotencyStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> find(String key) {
        String v = redis.opsForValue().get(key);
        return Optional.ofNullable(v);
    }

    @Override
    public boolean putIfAbsent(String key, String payload, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, payload, ttl);
        return Boolean.TRUE.equals(ok);
    }
}
