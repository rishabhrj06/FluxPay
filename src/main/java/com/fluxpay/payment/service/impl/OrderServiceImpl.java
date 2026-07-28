package com.fluxpay.payment.service.impl;

import com.fluxpay.common.enums.OrderStatus;
import com.fluxpay.common.exception.BusniessRuleViolationException;
import com.fluxpay.common.exception.DuplicateResourceException;
import com.fluxpay.common.exception.ResourceNotFoundException;
import com.fluxpay.payment.dto.request.CreateOrderRequest;
import com.fluxpay.payment.dto.response.OrderResponse;
import com.fluxpay.payment.dto.response.PaymentResponse;
import com.fluxpay.payment.entity.OrderRecord;
import com.fluxpay.payment.entity.Payment;
import com.fluxpay.payment.mapper.OrderMapper;
import com.fluxpay.payment.mapper.PaymentMapper;
import com.fluxpay.payment.repository.OrderRepository;
import com.fluxpay.payment.repository.PaymentRepository;
import com.fluxpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

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
                .status(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER", orderId));

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER", orderId));

        if(order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.PAID){
            throw new BusniessRuleViolationException("ORDER_CAN'T_BE_CANCELED",
                    "order can't be cancelled with the order status: " + order.getStatus().name()
            );
        }
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER", orderId));

        List<Payment> paymentList = paymentRepository.findByOrder_Id(order);

//        return paymentList.stream()
//                .map(paymentMapper::toResponse)
//                .toList();
        return paymentMapper.toListResponse(paymentList); //but it doesn't make sense because eventually, it is going to generate helper method to map single entity at a time
    }

}
