package com.fluxpay.vault.service.impl;

import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.CardBrand;
import com.fluxpay.common.exception.ResourceNotFoundException;
import com.fluxpay.common.utlis.RandomizerUtil;
import com.fluxpay.payment.processor.PaymentProcessorRouter;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import com.fluxpay.vault.config.VaultEncryptionConfig;
import com.fluxpay.vault.dto.request.TokenizeRequest;
import com.fluxpay.vault.dto.response.TokenizeResponse;
import com.fluxpay.vault.entity.CardToken;
import com.fluxpay.vault.entity.VaultCard;
import com.fluxpay.vault.repository.CardTokenRepository;
import com.fluxpay.vault.repository.VaultCardRepository;
import com.fluxpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse createToken(UUID merchantId, TokenizeRequest request) {
        String lastFour = request.pan().substring(request.pan().length() - 4);
        String bin = request.pan().substring(0, 6);
        CardBrand brand = detectBrand(bin);

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncrypter(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = vaultCardRepository.save(VaultCard.builder()
                .bin(bin)
                .cardHolderName(request.cardHolderName())
                .brand(brand)
                .lastFour(lastFour)
                .expiryMonth(String.valueOf(request.expiryMonth()))
                .expiryYear(String.valueOf(request.expiryYear()))
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .build());

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .merchant(merchantId)
                .customer(request.customerId())
                .build());

        return new TokenizeResponse(token, brand, lastFour, request.expiryMonth(), request.expiryYear());
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("TOKEN", "for token: " + token));

        byte[] panBytes = null;
        VaultCard vaultCard = cardToken.getVaultCard();

        try{
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());

            panBytes = VaultEncryptionConfig.panEncrypter(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest request = PaymentProcessorRequest.forCard(
                    paymentId, pan, expiry, methodDetails, amount
            );

            PaymentProcessorResponse response = paymentProcessorRouter.process(request);

            log.info("Vault Charge registered: {}****", token.substring(0, 4));
            return response;
        }catch (Exception e){
            log.warn("Vault charge failed for token: {}****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        }finally {
            if(panBytes != null) Arrays.fill(panBytes, (byte) 0);
        }

    }

    private CardBrand detectBrand(String pan) {
        if(pan == null || pan.length() < 4) return CardBrand.UNKNOWN;

        if(pan.startsWith("4")) return CardBrand.VISA;

        int firstTwo = Integer.parseInt(pan.substring(0, 2));
        if(firstTwo >= 51 && firstTwo <= 55) return CardBrand.MASTERCARD;

        int firstFour = Integer.parseInt(pan.substring(0, 4));
        if(firstFour >= 2221 && firstFour <= 2720) return CardBrand.MASTERCARD;

        if(pan.startsWith("34") || pan.startsWith("37")) return CardBrand.AMERICAN_EXPRESS;

        if(pan.startsWith("60") || pan.startsWith("6521") ||
            pan.startsWith("6522") || pan.startsWith("508")) return CardBrand.RUPAY;

        return CardBrand.UNKNOWN;
    }
}
