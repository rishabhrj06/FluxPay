package com.fluxpay.common.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "fixed")
public class FixedWindow implements RateLimiter{

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds) {

        String rateKey = "rate_limit:" + key;
        Long count = stringRedisTemplate.opsForValue().increment(rateKey);

        if(count == null) return RateLimitResult.allowed(maxRequestAllowed);

        if(count == 1) {
            stringRedisTemplate.expire(rateKey, Duration.ofSeconds(windowSeconds));
        }

        if(count > maxRequestAllowed) {
            Long ttl = stringRedisTemplate.getExpire(rateKey, TimeUnit.SECONDS);
            int retryAfterSeconds = ttl != null && ttl > 0 ? ttl.intValue() : (int) windowSeconds;
            return RateLimitResult.denied(retryAfterSeconds);
        }

        return RateLimitResult.allowed((int) (maxRequestAllowed - count));
    }
}
