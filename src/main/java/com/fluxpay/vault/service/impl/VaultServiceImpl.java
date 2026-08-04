package com.fluxpay.vault.service.impl;

import com.fluxpay.common.enums.CardBrand;
import com.fluxpay.common.utlis.RandomizerUtil;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;

    @Override
    @Transactional
    public TokenizeResponse createToken(UUID merchantId, TokenizeRequest request) {
        String lastFour = request.pan().substring(request.pan().length() - 4);
        String bin = request.pan().substring(0, 6);
        CardBrand brand = detectBrand(bin);

        // we will use the same dek to encrypt and decrypt so for encrypting it we need to use master key
        byte[] dek = KeyGenerators.secureRandom(32).generateKey(); //Data Encryption key this just returning a byte array of 32byte -> 256 bits AES supports 128,192,256
        byte[] encryptedPan = VaultEncryptionConfig.panEncrypter(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));  //AES works on bytes[]

        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .bin(bin)
                .cardHolderName(request.cardHolderName())
                .brand(brand)
                .lastFour(lastFour)
                .expiryMonth(String.valueOf(request.expiryMonth()))
                .expiryYear(String.valueOf(request.expiryYear()))
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .build();

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .merchant(merchantId)
                .customer(request.customerId())
                .build());

        return new TokenizeResponse(token, brand, lastFour, request.expiryMonth(), request.expiryYear());
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
