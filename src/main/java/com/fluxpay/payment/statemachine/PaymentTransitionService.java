package com.fluxpay.payment.statemachine;

import com.fluxpay.common.enums.PaymentActor;
import com.fluxpay.common.enums.PaymentEvent;
import com.fluxpay.common.enums.PaymentStatus;
import com.fluxpay.payment.entity.Payment;
import com.fluxpay.payment.entity.PaymentTransitionLog;
import com.fluxpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent) {
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), paymentEvent);
        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(paymentEvent)
                .actor(PaymentActor.SYSTEM)
                .occurredAt(LocalDateTime.now())
                .toStatus(next)
                .build();

        payment.setStatus(next);
        paymentTransitionLogRepository.save(log);
        return next;
    }
}
