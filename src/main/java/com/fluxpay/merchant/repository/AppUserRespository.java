package com.fluxpay.merchant.repository;

import com.fluxpay.merchant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRespository extends JpaRepository<AppUser, UUID> {
}
