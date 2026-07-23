package com.fluxpay.merchant.controller;

import com.fluxpay.merchant.dto.request.CreateApiKeyRequest;
import com.fluxpay.merchant.dto.response.ApiKeyCreateResponse;
import com.fluxpay.merchant.dto.response.ApiKeyResponse;
import com.fluxpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchant/{merchantId}/api-key")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@PathVariable UUID merchantId, @Valid @RequestBody CreateApiKeyRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.createApiKey(merchantId, request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getAllApiKeys(@PathVariable UUID merchantId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(apiKeyService.getAllApiKeys(merchantId));
    }

    @DeleteMapping("/key-id/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID merchantId, @PathVariable UUID keyId){
        apiKeyService.revoke(merchantId, keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rotate-key/{keyId}")
    public ResponseEntity<ApiKeyCreateResponse> rotateApiKey(@PathVariable UUID merchantId, @PathVariable UUID keyId){
        return ResponseEntity.ok(apiKeyService.rotateApiKey(merchantId, keyId));
    }

}
