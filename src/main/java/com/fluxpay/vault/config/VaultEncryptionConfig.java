package com.fluxpay.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

@Configuration
public class VaultEncryptionConfig {

    @Value("${vault.master-key}")
    private String masterKey;

    public static BytesEncryptor panEncrypter(byte[] dek){  //AES doesn't understand byte[] directly needs SecretKey
        SecretKeySpec decKey = new SecretKeySpec(dek, "AES"); // This simply wraps our dek bytes into java AES Object
        return new AesBytesEncryptor(decKey, KeyGenerators.secureRandom(12),    // takes (AES key object, IV generator, algo)
                AesBytesEncryptor.CipherAlgorithm.GCM); // nothing encrypted yet just prepared it
    }
    /*
        IV (Initialization vector) -> without it, the encryption may generate similar encrypted pan I mean for different
         user can have same encrypted pan that can be guessed that they have sam pan numbers. we use 12 bytes because it is
          recommended for AES GCM mode, GCM(modern) and CBC are modes which use AES to encrypt
          GCM (Galois/Counter Mode) -> Uses Encryption and Authentication, if one encrypted byte change it
           denies to decrypting garbage, says Authentication failed;
          CBC (Cipher block chaining) -> block wise encryption, problem -> if someone modifies one byte it
           can give garbage, it doesn't prove the data wasn't modified
    */

    @Bean
    public BytesEncryptor dekEncryptor(){
        byte[] masterKeyBytes = HexFormat.of().parseHex(masterKey);
        SecretKeySpec masterKeyObj = new SecretKeySpec(masterKeyBytes, "AES");
        return new AesBytesEncryptor(masterKeyObj, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }
}
