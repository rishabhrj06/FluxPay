package com.fluxpay.common.idempotancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;
    private static final String PREFIX = "idempotency: ";

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try{
            Boolean set = redis.opsForValue().setIfAbsent(PREFIX+key, IN_PROGRESS, ttl);
            return Boolean.TRUE.equals(set);
        } catch(DataAccessException ex) {
            log.warn("Idempotency store unavailable for key {}", key, ex);
            return true; //fail open
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redis.opsForValue().set(PREFIX+key, value, ttl);
        } catch (DataAccessException ex) {
            log.warn("Failed to persist, failing open for key {}", key, ex);
        }
    }

    @Override
    public Optional<String> get(String key) {
        try{
            return Optional.ofNullable(redis.opsForValue().get(PREFIX+key));
        } catch (DataAccessException ex) {
            log.warn("Failed to persist, failing open for key {}", key, ex);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try{
            redis.delete(PREFIX+key);
        } catch (DataAccessException ex) {
            log.warn("Failed to delete for key {}", key, ex);
        }
    }
}
