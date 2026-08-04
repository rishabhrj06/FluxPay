package com.fluxpay.payment.service.impl;

import com.fluxpay.common.enums.OrderStatus;
import com.fluxpay.common.enums.PaymentEvent;
import com.fluxpay.common.enums.PaymentStatus;
import com.fluxpay.common.exception.BusniessRuleViolationException;
import com.fluxpay.common.exception.ResourceNotFoundException;
import com.fluxpay.payment.dto.request.PaymentInitRequest;
import com.fluxpay.payment.dto.response.PaymentResponse;
import com.fluxpay.payment.entity.OrderRecord;
import com.fluxpay.payment.entity.Payment;
import com.fluxpay.payment.gateway.PaymentGatewayRouter;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import com.fluxpay.payment.mapper.PaymentMapper;
import com.fluxpay.payment.mapper.PaymentMapperImpl;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import com.fluxpay.payment.repository.OrderRepository;
import com.fluxpay.payment.repository.PaymentRepository;
import com.fluxpay.payment.service.PaymentService;
import com.fluxpay.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER", request.orderId()));

        if(order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.ATTEMPTED){
            throw new BusniessRuleViolationException("ORDER_NOT_PAYBLE", "Order can't be paid for status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();

        paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),
                merchantId,
                request.orderId(),
                order.getAmount(),
                request.method(),
                request.methodDetails()
        );

        PaymentResult result = paymentGatewayRouter.initiatePayment(paymentRequest);

        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
//                payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success-> {

            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", "payment not found for paymentId: " + paymentId));

//        payment.setStatus(PaymentStatus.CAPTURING);
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if(paymentResult instanceof PaymentResult.Success success){
//            payment.setStatus(PaymentStatus.CAPTURED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment capture for payment Id: {}", paymentId);
        }
        else if(paymentResult instanceof PaymentResult.Failure failure){
//           payment.setStatus(PaymentStatus.AUTHORIZED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
           payment.setErrorDescription(failure.errorDescription());
           payment.setErrorCode(failure.errorCode());
           log.warn("Payment capture failed for payment Id: {}", paymentId);
        }

        payment = paymentRepository.save(payment);
        return paymentMapper.toResponse(payment);
    }

}
