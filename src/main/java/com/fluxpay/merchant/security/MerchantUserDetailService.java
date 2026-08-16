package com.fluxpay.merchant.security;

import com.fluxpay.common.exception.ResourceNotFoundException;
import com.fluxpay.merchant.repository.AppUserRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantUserDetailService implements UserDetailsService {

    private final AppUserRespository appUserRespository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRespository.findByEmail(email)
                .orElseThrow(() ->  new ResourceNotFoundException("USER", email));
    }
}
