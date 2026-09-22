package com.fluxpay.common.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.rate-limit.method",
        havingValue = "sliding-lua"
)
public class SlidingWindowLua implements RateLimiter {

    private final StringRedisTemplate redis;

    private static final String SLIDING_WINDOW_LUA = """
            local key = KEYS[1]

            local currTime = tonumber(ARGV[1])
            local windowStart = tonumber(ARGV[2])
            local maxRequestAllowed = tonumber(ARGV[3])
            local ttl = tonumber(ARGV[4])
            local requestId = ARGV[5]

            -- Remove expired requests
            redis.call(
                    'ZREMRANGEBYSCORE',
                    key,
                    '-inf',
                    windowStart
            )

            -- Count current requests
            local requestCount = redis.call(
                    'ZCARD',
                    key
            )

            -- Check rate limit
            if requestCount >= maxRequestAllowed then

                -- Get oldest request timestamp
                local oldestRequest = redis.call(
                        'ZRANGE',
                        key,
                        0,
                        0,
                        'WITHSCORES'
                )

                local oldestTimeStamp = 0

                if #oldestRequest > 0 then
                    oldestTimeStamp =
                            tonumber(oldestRequest[2])
                end

                return {
                        0,
                        0,
                        oldestTimeStamp
                }
            end

            -- Add current request
            redis.call(
                    'ZADD',
                    key,
                    currTime,
                    requestId
            )

            -- Set TTL
            redis.call(
                    'EXPIRE',
                    key,
                    ttl
            )

            -- Calculate remaining requests
            local remaining =
                    maxRequestAllowed
                    - requestCount
                    - 1

            return {
                    1,
                    remaining,
                    0
            }
            """;

    private final RedisScript<List> slidingWindowScript = RedisScript.of(SLIDING_WINDOW_LUA, List.class);

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds) {

        try {
            long currTime = System.currentTimeMillis();
            long windowStart = currTime - (windowSeconds * 1000L);
            String requestId = UUID.randomUUID().toString();
            String redisKey = "rate_limit:" + key;

            List result = redis.execute(
                    slidingWindowScript,
                    List.of(redisKey),
                    String.valueOf(currTime),
                    String.valueOf(windowStart),
                    String.valueOf(maxRequestAllowed),
                    String.valueOf(windowSeconds + 1),
                    requestId
            );

            if (result == null || result.isEmpty()) {

                // Redis unavailable → fail open
                return RateLimitResult.allowed(
                        maxRequestAllowed
                );
            }

            boolean allowed = ((Number) result.get(0)).intValue() == 1;
            int remaining = ((Number) result.get(1)).intValue();
            long oldestTimeStamp = ((Number) result.get(2)).longValue();

            if (!allowed) {
                int retryAfterSeconds = oldestTimeStamp > 0 ?
                        (int) Math.max(1, Math.ceil((oldestTimeStamp + (windowSeconds * 1000L) - currTime) / 1000.0))
                        : windowSeconds;

                return RateLimitResult.denied(
                        retryAfterSeconds
                );
            }

            return RateLimitResult.allowed(
                    remaining
            );

        } catch (Exception e) {
            return RateLimitResult.allowed(
                    maxRequestAllowed
            );
        }
    }
}