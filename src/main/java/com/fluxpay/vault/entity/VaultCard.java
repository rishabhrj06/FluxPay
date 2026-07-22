package com.fluxpay.vault.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
public class VaultCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false, length = 6)
    private String bin;     // first 6 digits Bank Identification Number (411111    Brand : Visa,Bank  : HDFC, Country : India, Type : Debit)

    @Column(nullable = false)
    private byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek; //random String to encrypt the pan (Via generate through randomizer or something)

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false, length = 2)
    private String expiryMonth;

    @Column(nullable = false, length = 4)
    private String expiryYear;

    @Column(nullable = false, length = 200)
    private String cardHolderName;

    private LocalDateTime deletedAt;
}
