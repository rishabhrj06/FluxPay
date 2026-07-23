package com.fluxpay.payment.service.impl;

import com.fluxpay.common.enums.OrderStatus;
import com.fluxpay.common.exception.DuplicateResourceException;
import com.fluxpay.payment.dto.request.CreateOrderRequest;
import com.fluxpay.payment.dto.response.OrderResponse;
import com.fluxpay.payment.entity.OrderRecord;
import com.fluxpay.payment.repository.OrderRepository;
import com.fluxpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    public OrderResponse createOrder(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())){
            throw new DuplicateResourceException("ORDER_EXIST", "order with receipt already exists" + request.receipt());
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .merchantId(merchantId)
                .amount(request.amount())
                .notes(request.notes())
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        return new OrderResponse(order.getId(),
                order.getMerchantId(),
                order.getReceipt(),
                order.getAmount(),
                order.getOrderStatus(),
                order.getAttempts(),
                order.getNotes(),
                null,
                order.getExpiresAt());
    }
}
