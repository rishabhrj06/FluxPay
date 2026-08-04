package com.fluxpay.vault.controller;

import com.fluxpay.vault.dto.request.TokenizeRequest;
import com.fluxpay.vault.dto.response.TokenizeResponse;
import com.fluxpay.vault.service.VaultService;
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
@RequestMapping("/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    UUID merchantId = UUID.fromString("c5d157b0-bad2-49cc-b295-aa8da04f8d3c");


    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> createToken(@Valid @RequestBody TokenizeRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.createToken(merchantId, request));
    }
}
