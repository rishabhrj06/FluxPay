package com.fluxpay.merchant.service.impl;

import com.fluxpay.common.enums.MerchantStatus;
import com.fluxpay.common.enums.UserRole;
import com.fluxpay.common.exception.DuplicateResourceException;
import com.fluxpay.common.exception.ResourceNotFoundException;
import com.fluxpay.merchant.dto.request.LoginRequest;
import com.fluxpay.merchant.dto.request.MerchantSignUpRequest;
import com.fluxpay.merchant.dto.response.LoginResponse;
import com.fluxpay.merchant.dto.response.MerchantResponse;
import com.fluxpay.merchant.entity.AppUser;
import com.fluxpay.merchant.entity.Merchant;
import com.fluxpay.merchant.repository.AppUserRespository;
import com.fluxpay.merchant.repository.MerchantRepository;
import com.fluxpay.merchant.security.JwtUtil;
import com.fluxpay.merchant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRespository appUserRespository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public MerchantResponse signUp(MerchantSignUpRequest request) {
        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("EMAIL_EXISTS", "Email already exists: " + request.email());
        }

        Merchant merchant = Merchant.builder()
                .name(request.name())
                .email(request.email())
                .businessName(request.businessName())
                .businessType(request.businessType())
                .merchantStatus(MerchantStatus.PENDING_KYC)
                .build();

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRespository.save(appUser);

        return new  MerchantResponse(
                merchant.getId(), merchant.getName(), merchant.getEmail(),
                merchant.getBusinessName(), merchant.getBusinessType(), merchant.getMerchantStatus()
        );

    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser appUser = appUserRespository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("EMAIL", request.email()));

        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().name());

        return new LoginResponse(token);

    }
}
