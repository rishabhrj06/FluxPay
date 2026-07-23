package com.fluxpay.payment.service;

import com.fluxpay.payment.dto.request.CreateOrderRequest;
import com.fluxpay.payment.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(UUID merchantId, CreateOrderRequest request);
}
