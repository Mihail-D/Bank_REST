package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardSearchDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardNumberGenerator;
import com.example.bankcards.specification.CardSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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

    // Тесты для функционала поиска карт

    @Test
    void searchCardsShouldReturnCardsMatchingCriteria() {
        // Given
        CardSearchDto searchDto = new CardSearchDto();
        searchDto.setStatus(CardStatus.ACTIVE);
        searchDto.setUserId(1L);

        List<Card> expectedCards = Arrays.asList(createTestCard(1L), createTestCard(2L));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCards(searchDto);

        // Then
        assertEquals(2, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void searchCardsShouldFilterByMaskWhenProvided() {
        // Given
        CardSearchDto searchDto = new CardSearchDto();
        searchDto.setMask("1234");

        Card card1 = createTestCard(1L);
        Card card2 = createTestCard(2L);
        List<Card> allCards = Arrays.asList(card1, card2);

        when(cardRepository.findAll()).thenReturn(allCards);
        when(cardEncryptionService.decrypt(anyString())).thenReturn("1234567890123456", "9876543210987654");
        when(cardEncryptionService.mask(anyString())).thenReturn("**** **** **** 3456", "**** **** **** 7654");

        // When
        List<Card> result = cardService.searchCards(searchDto);

        // Then
        assertEquals(0, result.size()); // Ни одна карта не соответствует маске "1234"
    }

    @Test
    void searchCardsByMaskShouldReturnMatchingCards() {
        // Given
        String mask = "1234";
        Card card1 = createTestCard(1L);
        Card card2 = createTestCard(2L);
        List<Card> allCards = Arrays.asList(card1, card2);

        when(cardRepository.findAll()).thenReturn(allCards);
        when(cardEncryptionService.decrypt(card1.getEncryptedNumber())).thenReturn("1234567890121234");
        when(cardEncryptionService.decrypt(card2.getEncryptedNumber())).thenReturn("9876543210987654");
        when(cardEncryptionService.mask("1234567890121234")).thenReturn("**** **** **** 1234");
        when(cardEncryptionService.mask("9876543210987654")).thenReturn("**** **** **** 7654");

        // When
        List<Card> result = cardService.searchCardsByMask(mask);

        // Then
        assertEquals(1, result.size());
        assertEquals(card1, result.get(0));
    }

    @Test
    void searchCardsByMaskShouldReturnEmptyListWhenMaskIsNull() {
        // When
        List<Card> result = cardService.searchCardsByMask(null);

        // Then
        assertEquals(0, result.size());
        verifyNoInteractions(cardRepository);
    }

    @Test
    void searchCardsByMaskShouldReturnEmptyListWhenMaskIsEmpty() {
        // When
        List<Card> result = cardService.searchCardsByMask("");

        // Then
        assertEquals(0, result.size());
        verifyNoInteractions(cardRepository);
    }

    @Test
    void searchCardsByOwnerNameShouldUseSpecification() {
        // Given
        String ownerName = "Иван";
        List<Card> expectedCards = Arrays.asList(createTestCard(1L));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCardsByOwnerName(ownerName);

        // Then
        assertEquals(1, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void searchCardsByOwnerNameShouldReturnEmptyListWhenNameIsNull() {
        // When
        List<Card> result = cardService.searchCardsByOwnerName(null);

        // Then
        assertEquals(0, result.size());
        verifyNoInteractions(cardRepository);
    }

    @Test
    void searchCardsByStatusAndOwnerShouldUseSpecificationWhenBothProvided() {
        // Given
        CardStatus status = CardStatus.ACTIVE;
        Long userId = 1L;
        List<Card> expectedCards = Arrays.asList(createTestCard(1L));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCardsByStatusAndOwner(status, userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void searchCardsByStatusAndOwnerShouldReturnAllCardsWhenNoCriteriaProvided() {
        // Given
        List<Card> allCards = Arrays.asList(createTestCard(1L), createTestCard(2L));
        when(cardRepository.findAll()).thenReturn(allCards);

        // When
        List<Card> result = cardService.searchCardsByStatusAndOwner(null, null);

        // Then
        assertEquals(2, result.size());
        assertEquals(allCards, result);
        verify(cardRepository).findAll();
    }

    @Test
    void searchCardsByStatusAndOwnerShouldUseSpecificationWhenOnlyStatusProvided() {
        // Given
        CardStatus status = CardStatus.BLOCKED;
        List<Card> expectedCards = Arrays.asList(createTestCard(1L));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCardsByStatusAndOwner(status, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void searchCardsByStatusAndOwnerShouldUseSpecificationWhenOnlyUserIdProvided() {
        // Given
        Long userId = 1L;
        List<Card> expectedCards = Arrays.asList(createTestCard(1L));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.searchCardsByStatusAndOwner(null, userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll(any(Specification.class));
    }

    // Вспомогательный метод для создания тестовых карт
    private Card createTestCard(Long id) {
        Card card = new Card();
        card.setId(id);
        card.setEncryptedNumber("encrypted" + id);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));

        User user = new User();
        user.setId(id);
        card.setUser(user);

        return card;
    }
}
