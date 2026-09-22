package com.fluxpay.common.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "token-bucket-lua")
public class TokenBucketLua implements RateLimiter {

    private final StringRedisTemplate redis;

    private static final String TOKEN_BUCKET_LUA = """
            local key = KEYS[1]

            local currTime = tonumber(ARGV[1])
            local maxRequestAllowed = tonumber(ARGV[2])
            local windowSeconds = tonumber(ARGV[3])

            -- Calculate how fast tokens should refill
            local refillRate =
                    maxRequestAllowed / windowSeconds

            -- Get current bucket state
            local tokens = redis.call(
                    'HGET',
                    key,
                    'tokens'
            )

            local lastRefillTime = redis.call(
                    'HGET',
                    key,
                    'lastRefillTime'
            )

            -- First request: create a full bucket
            if tokens == false then
                    tokens = maxRequestAllowed
                    lastRefillTime = currTime
            else
                    tokens = tonumber(tokens)
                    lastRefillTime = tonumber(lastRefillTime)

                    -- Calculate elapsed time
                    local elapsedSeconds =
                            (currTime - lastRefillTime) / 1000

                    -- Add tokens based on elapsed time
                    tokens =
                            tokens
                            + (elapsedSeconds * refillRate)

                    -- Bucket cannot contain more than capacity
                    if tokens > maxRequestAllowed then
                            tokens = maxRequestAllowed
                    end

                    lastRefillTime = currTime
            end

            -- Check whether a token is available
            if tokens >= 1 then

                    -- Consume one token
                    tokens = tokens - 1

                    redis.call(
                            'HSET',
                            key,
                            'tokens',
                            tokens,
                            'lastRefillTime',
                            lastRefillTime
                    )

                    -- Keep Redis key for cleanup
                    redis.call(
                            'EXPIRE',
                            key,
                            windowSeconds + 1
                    )

                    return {
                            1,
                            math.floor(tokens),
                            0
                    }
            end

            -- No token available
            local retryAfterSeconds =
                    math.ceil(
                            (1 - tokens) / refillRate
                    )

            redis.call(
                    'HSET',
                    key,
                    'tokens',
                    tokens,
                    'lastRefillTime',
                    lastRefillTime
            )

            redis.call(
                    'EXPIRE',
                    key,
                    windowSeconds + 1
            )

            return {
                    0,
                    0,
                    math.max(retryAfterSeconds, 1)
            }
            """;

    private final RedisScript<List> tokenBucketScript =
            RedisScript.of(
                    TOKEN_BUCKET_LUA,
                    List.class
            );

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds) {
        try {
            long currTime = System.currentTimeMillis();
            String redisKey = "rate_limit:" + key;

            List result = redis.execute(
                    tokenBucketScript,
                    List.of(redisKey),
                    String.valueOf(currTime),
                    String.valueOf(maxRequestAllowed),
                    String.valueOf(windowSeconds)
            );

            if (result == null || result.isEmpty()) return RateLimitResult.allowed(maxRequestAllowed); // Redis unavailable → fail open

            boolean allowed = ((Number) result.get(0)).intValue() == 1;
            int remaining = ((Number) result.get(1)).intValue();
            int retryAfterSeconds = ((Number) result.get(2)).intValue();

            if (!allowed) return RateLimitResult.denied(retryAfterSeconds);

            return RateLimitResult.allowed(remaining);

        } catch (Exception e) {
            // Redis unavailable → fail open
            return RateLimitResult.allowed(maxRequestAllowed);
        }
    }
}