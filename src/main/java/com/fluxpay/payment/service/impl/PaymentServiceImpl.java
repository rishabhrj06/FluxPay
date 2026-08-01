package com.fluxpay.payment.service.impl;

import com.fluxpay.common.enums.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;

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
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }
}
