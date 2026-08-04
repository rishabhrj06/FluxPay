package com.fluxpay.vault.repository;

import com.fluxpay.vault.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardTokenRepository extends JpaRepository<CardToken, UUID> {
}
