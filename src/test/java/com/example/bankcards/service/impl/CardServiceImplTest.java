package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceImplTest {
    private CardRepository cardRepository;
    private CardNumberGenerator cardNumberGenerator;
    private CardEncryptionService cardEncryptionService;
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        cardNumberGenerator = Mockito.mock(CardNumberGenerator.class);
        cardEncryptionService = Mockito.mock(CardEncryptionService.class);
        cardService = new CardServiceImpl(cardRepository, cardNumberGenerator, cardEncryptionService);
    }

    @Test
    void createCardShouldGenerateAndSaveCard() {
        User user = new User();
        String generatedNumber = "1234567890123452";
        String encryptedNumber = "encrypted";
        when(cardNumberGenerator.generateUniqueCardNumber()).thenReturn(generatedNumber);
        when(cardEncryptionService.encrypt(generatedNumber)).thenReturn(encryptedNumber);
        Card savedCard = new Card();
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        Card result = cardService.createCard(user);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card card = cardCaptor.getValue();

        assertEquals(user, card.getUser());
        assertEquals(encryptedNumber, card.getEncryptedNumber());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(LocalDate.now().plusYears(3), card.getExpirationDate());
        assertSame(savedCard, result);
    }
}
