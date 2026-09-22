package com.fluxpay.payment.controller;

import com.fluxpay.merchant.security.MerchantContext;
import com.fluxpay.payment.dto.request.CreateOrderRequest;
import com.fluxpay.payment.dto.response.OrderResponse;
import com.fluxpay.payment.service.OrderService;
import com.fluxpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(merchantContext.getMerchantId(), request));
    }
}
