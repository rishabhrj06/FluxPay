package com.fluxpay.operations.entity;

import com.fluxpay.common.entity.BaseEntity;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class SettlementPaymentId{
    private UUID settlementId;
    private UUID paymentId;
}
