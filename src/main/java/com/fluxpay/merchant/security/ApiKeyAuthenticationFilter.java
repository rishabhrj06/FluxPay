package com.fluxpay.merchant.security;

import com.fluxpay.merchant.entity.ApiKey;
import com.fluxpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyRepository apiKeyRepository;
    private final MerchantContext merchantContext;

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

            ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
                    .orElseThrow(() -> new BadCredentialsException("Invalid api key"));

            if(!apiKey.isEnabled() || !secretMatches(rawSecret, apiKey)){
                throw new BadCredentialsException("Invalid api key");
            }

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setKeyId(apiKey.getKeyId());
            merchantContext.setMerchantId(UUID.fromString(apiKey.getKeyId()));

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private boolean secretMatches(String rawSecret, ApiKey apiKey) {
        if(BCRYPT.matches(rawSecret, apiKey.getKeySecretHash())) return true;

        boolean isInGracePeriod = apiKey.getGracePeriodExpiresAt() != null &&
                LocalDateTime.now().isBefore(apiKey.getGracePeriodExpiresAt());

        return isInGracePeriod && apiKey.getPreviousKeySecretHash() != null &&
                BCRYPT.matches(rawSecret, apiKey.getPreviousKeySecretHash());
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
