package com.fluxpay.merchant.repository;

import com.fluxpay.merchant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRespository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
}
