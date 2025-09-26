package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import com.example.bankcards.exception.EncryptionFailureException;

@Service
public class CardEncryptionService {
    private final String secretKey;

    public CardEncryptionService(@Value("${card.encryption.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String cardNumber) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EncryptionFailureException("Ошибка шифрования номера карты", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new EncryptionFailureException("Ошибка дешифрования номера карты", e);
        }
    }

    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) return null;
        return "**** **** **** " + cardNumber.substring(12);
    }
}
