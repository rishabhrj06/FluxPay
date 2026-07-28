package com.fluxpay.payment.entity;

import com.fluxpay.common.entity.BaseEntity;
import com.fluxpay.common.enums.PaymentEvent;
import com.fluxpay.common.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payment_transition_log",
        indexes = {
                @Index(name = "idx_payment_transition_log_payment_id", columnList = "payment_id")
        }
)
public class PaymentTransitionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", length = 30, nullable = false)
    private PaymentEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name = "toStatus", length = 30)
    private PaymentStatus toStatus;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
