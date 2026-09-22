package com.fluxpay.common.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "token-bucket")
public class TokenBucket implements RateLimiter {

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds) {

        long currTime = System.currentTimeMillis();
        String redisKey = "rate_limit:" + key;

        double refillRate = (double) maxRequestAllowed / windowSeconds;

        Map<Object, Object> bucket = redis.opsForHash().entries(redisKey);

        double tokens;
        long lastRefillTime;

        if (bucket.isEmpty()) {
            tokens = maxRequestAllowed; // First request: bucket starts full
            lastRefillTime = currTime;
        } else {
            tokens = Double.parseDouble(bucket.get("tokens").toString());

            lastRefillTime = Long.parseLong(bucket.get("lastRefillTime").toString());

            long elapsedMillis = currTime - lastRefillTime; // Calculate elapsed time

            double elapsedSeconds = elapsedMillis / 1000.0;

            tokens += elapsedSeconds * refillRate;  // Add newly generated tokens

            tokens = Math.min(tokens, maxRequestAllowed);    // Bucket cannot exceed its capacity

            lastRefillTime = currTime;   // Update last refill time
        }

        // Check whether a token is available
        if (tokens < 1) {
            double missingTokens = 1 - tokens;

            int retryAfterSeconds = (int) Math.ceil(missingTokens / refillRate);

            return RateLimitResult.denied(Math.max(retryAfterSeconds, 1));
        }

        // Consume one token
        tokens--;

        // Save bucket state
        redis.opsForHash().putAll(
                redisKey,
                Map.of(
                        "tokens", String.valueOf(tokens),
                        "lastRefillTime",
                        String.valueOf(lastRefillTime)
                )
        );

        // Cleanup unused Redis keys
        redis.expire(
                redisKey,
                Duration.ofSeconds(windowSeconds + 1)
        );

        return RateLimitResult.allowed(
                (int) Math.floor(tokens)
        );
    }
}