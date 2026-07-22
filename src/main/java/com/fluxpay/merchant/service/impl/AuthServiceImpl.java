package com.fluxpay.merchant.service.impl;

import com.fluxpay.common.enums.MerchantStatus;
import com.fluxpay.common.enums.UserRole;
import com.fluxpay.common.exception.DuplicateResourceException;
import com.fluxpay.merchant.dto.request.MerchantSignUpRequest;
import com.fluxpay.merchant.dto.response.MerchantResponse;
import com.fluxpay.merchant.entity.AppUser;
import com.fluxpay.merchant.entity.Merchant;
import com.fluxpay.merchant.repository.AppUserRespository;
import com.fluxpay.merchant.repository.MerchantRepository;
import com.fluxpay.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRespository appUserRespository;

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
                .password(request.password()) // TODO: has to encrypt it
                .role(UserRole.OWNER)
                .build();

        appUserRespository.save(appUser);

        return new  MerchantResponse(
                merchant.getId(), merchant.getName(), merchant.getEmail(),
                merchant.getBusinessName(), merchant.getBusinessType(), merchant.getMerchantStatus()
        );

    }
}
