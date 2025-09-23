package com.example.bankcards.service;

import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class CardNumberGeneratorTest {
    private CardRepository cardRepository;
    private CardEncryptionService cardEncryptionService;
    private CardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        cardEncryptionService = Mockito.mock(CardEncryptionService.class);
        generator = new CardNumberGenerator(cardRepository, cardEncryptionService);
    }

    @Test
    void generatedCardNumberShouldBe16Digits() {
        Mockito.when(cardEncryptionService.encrypt(anyString())).thenReturn("encrypted");
        Mockito.when(cardRepository.findByEncryptedNumber("encrypted")).thenReturn(java.util.Optional.empty());
        String number = generator.generateUniqueCardNumber();
        assertEquals(16, number.length());
        assertTrue(number.matches("\\d{16}"));
    }

    @Test
    void generatorShouldRetryIfNumberExists() {
        Mockito.when(cardEncryptionService.encrypt(anyString())).thenReturn("encrypted");
        Mockito.when(cardRepository.findByEncryptedNumber("encrypted"))
                .thenReturn(java.util.Optional.of(Mockito.mock(com.example.bankcards.entity.Card.class)))
                .thenReturn(java.util.Optional.empty());
        String number = generator.generateUniqueCardNumber();
        assertEquals(16, number.length());
        assertTrue(number.matches("\\d{16}"));
    }

    @Test
    void generatedCardNumberShouldPassLuhnCheck() {
        Mockito.when(cardEncryptionService.encrypt(anyString())).thenReturn("encrypted");
        Mockito.when(cardRepository.findByEncryptedNumber("encrypted")).thenReturn(java.util.Optional.empty());
        String number = generator.generateUniqueCardNumber();
        assertTrue(CardNumberGenerator.isValidLuhn(number), "Номер карты должен проходить проверку Луна");
    }

    @Test
    void luhnAlgorithmShouldValidateKnownNumbers() {
        // Валидные номера
        assertTrue(CardNumberGenerator.isValidLuhn("4539578763621486"));
        assertTrue(CardNumberGenerator.isValidLuhn("6011000990139424"));
        // Невалидные номера
        assertFalse(CardNumberGenerator.isValidLuhn("1234567890123456"));
        assertFalse(CardNumberGenerator.isValidLuhn("1111111111111111"));
    }
}
