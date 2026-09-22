package com.fluxpay.merchant.security;

import com.fluxpay.common.exception.RateLimitException;
import com.fluxpay.common.rate_limit.RateLimitResult;
import com.fluxpay.common.rate_limit.RateLimiter;
import com.fluxpay.merchant.cache.ApiKeyCache;
import com.fluxpay.merchant.cache.ApiKeyCacheEntry;
import com.fluxpay.merchant.entity.ApiKey;
import com.fluxpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyRepository apiKeyRepository;
    private final MerchantContext merchantContext;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.max-req-allowed}")
    private Integer maxRequestAllowed;

    @Value("${app.rate-limit.use-case.api-key.window-time}")
    private Integer windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Api Key Authentication Filter, request URI: {}", request.getRequestURI());

        try{
            String header = request.getHeader("Authorization");
            if(header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decode(header);
            if(credentials == null) {
                throw new BadCredentialsException("Malformed credentials");
            }
            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKeyCacheEntry = apiKeyCache.get(keyId).orElseGet(() -> loadAndCache(keyId));

            if(!apiKeyCacheEntry.enabled() || !secretMatches(rawSecret, apiKeyCacheEntry)){
                throw new BadCredentialsException("Invalid api key");
            }

            RateLimitResult rateLimitResult = rateLimiter.tryAcquire("api_key:" + keyId, maxRequestAllowed, windowSeconds);

            if(!rateLimitResult.allowed()) {
                log.warn("Too many requests for keyId: {}", keyId);
                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
            }

            response.setHeader("X-Rate-Limit", String.valueOf(maxRequestAllowed));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(rateLimitResult.remaining()));

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setKeyId(apiKeyCacheEntry.keyId());
            merchantContext.setMerchantId(apiKeyCacheEntry.merchantId());

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
                .orElseThrow(() -> new BadCredentialsException("Invalid api key"));

        ApiKeyCacheEntry entry = new ApiKeyCacheEntry(
                apiKey.getKeyId(),
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getGracePeriodExpiresAt(),
                apiKey.getEnvironment(),
                apiKey.isEnabled(),
                apiKey.getMerchant().getId()
        );
        apiKeyCache.put(keyId, entry);
        return entry;
    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey) {
        if(BCRYPT.matches(rawSecret, apiKey.keySecretHash())) return true;

        return apiKey.isInGracePeriod() && apiKey.previousKeySecretHash() != null &&
                BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash());
    }

    //Authorization: Basic api_snfieomc:secret_ksjdcdkc

    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if(colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
}
