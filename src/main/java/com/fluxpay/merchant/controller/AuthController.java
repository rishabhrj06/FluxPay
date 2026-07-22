package com.fluxpay.merchant.controller;

import com.fluxpay.merchant.dto.request.MerchantSignUpRequest;
import com.fluxpay.merchant.dto.response.MerchantResponse;
import com.fluxpay.merchant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<MerchantResponse> signUp(@RequestBody @Valid MerchantSignUpRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.signUp(request)
        );
    }
}
