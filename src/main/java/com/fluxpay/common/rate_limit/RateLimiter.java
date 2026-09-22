package com.fluxpay.common.rate_limit;

public interface RateLimiter {
    RateLimitResult tryAcquire(String key, int maxRequestAllowed, int windowSeconds);
}
