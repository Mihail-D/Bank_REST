package com.example.bankcards.service;

import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class CardNumberGeneratorTest {
    private CardRepository cardRepository;
    private CardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        generator = new CardNumberGenerator(cardRepository);
    }

    @Test
    void generatedCardNumberShouldBe16Digits() {
        Mockito.when(cardRepository.existsByNumber(anyString())).thenReturn(false);
        String number = generator.generateUniqueCardNumber();
        assertEquals(16, number.length());
        assertTrue(number.matches("\\d{16}"));
    }

    @Test
    void generatorShouldRetryIfNumberExists() {
        Mockito.when(cardRepository.existsByNumber(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        String number = generator.generateUniqueCardNumber();
        assertEquals(16, number.length());
        assertTrue(number.matches("\\d{16}"));
    }

    @Test
    void generatedCardNumberShouldPassLuhnCheck() {
        Mockito.when(cardRepository.existsByNumber(anyString())).thenReturn(false);
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
