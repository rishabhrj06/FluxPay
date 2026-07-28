package com.fluxpay.payment.service;

import com.fluxpay.payment.dto.request.CreateOrderRequest;
import com.fluxpay.payment.dto.response.OrderResponse;
import com.fluxpay.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(UUID merchantId, CreateOrderRequest request);

    OrderResponse getById(UUID merchantId, UUID id);

    OrderResponse cancelOrder(UUID merchantId, UUID id);

    List<PaymentResponse> listPayments(UUID merchantId, UUID id);
}
