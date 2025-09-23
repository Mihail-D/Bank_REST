package com.example.bankcards.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class CardEncryptionServiceTest {
    @Test
    void testEncryptDecrypt() {
        CardEncryptionService service = new CardEncryptionService("1234567890abcdef");
        String cardNumber = "1234567890123456";
        String encrypted = service.encrypt(cardNumber);
        assertNotNull(encrypted);
        String decrypted = service.decrypt(encrypted);
        assertEquals(cardNumber, decrypted);
    }

    @Test
    void testMask() {
        CardEncryptionService service = new CardEncryptionService("1234567890abcdef");
        String cardNumber = "1234567890123456";
        String masked = service.mask(cardNumber);
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void testDecryptInvalid() {
        CardEncryptionService service = new CardEncryptionService("1234567890abcdef");
        assertNull(service.decrypt("invalid_base64"));
    }
}

