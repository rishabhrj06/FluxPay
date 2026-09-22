package com.fluxpay.common.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
public class SlidingWindow implements RateLimiter{

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds) {

        long currTime = System.currentTimeMillis();
        long windowStart = currTime - (windowSeconds * 1000L);

        String redisKey = "rate_limit:" + key;

        redis.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

        Long requestCount = redis.opsForZSet().zCard(redisKey);

        if(requestCount != null && requestCount >= maxRequestAllowed) {
            var oldestRequest = redis.opsForZSet().rangeWithScores(redisKey, 0, 0);

            if(oldestRequest != null && !oldestRequest.isEmpty()) {
                Double oldestTimeStamp = oldestRequest.iterator().next().getScore();
                long retryAfterMillis = 1;

                if(oldestTimeStamp != null) {
                    retryAfterMillis =
                            oldestTimeStamp.longValue() + (windowSeconds * 1000L) - currTime;
                }

                int retryAfterSeconds = (int) Math.ceil((retryAfterMillis / 1000.0));
                return RateLimitResult.denied(retryAfterSeconds);
            }
            return RateLimitResult.denied(windowSeconds);
        }

        redis.opsForZSet()
                .add(redisKey, UUID.randomUUID().toString(), currTime);

        redis.expire(redisKey, Duration.ofSeconds(windowSeconds + 1));

        int remaining = maxRequestAllowed - ((requestCount == null) ? 0 : requestCount.intValue()) - 1;

        return RateLimitResult.allowed(Math.max(remaining, 0));
    }
}
