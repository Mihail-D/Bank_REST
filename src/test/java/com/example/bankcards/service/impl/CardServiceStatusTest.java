package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.security.SecurityUtil;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceStatusTest {
    private CardRepository cardRepository;
    private HistoryRepository historyRepository;
    private CardNumberGenerator cardNumberGenerator;
    private CardEncryptionService cardEncryptionService;
    private SecurityUtil securityUtil;
    private CardServiceImpl service;

    private Card card;

    @BeforeEach
    void init() {
        cardRepository = Mockito.mock(CardRepository.class);
        historyRepository = Mockito.mock(HistoryRepository.class);
        cardNumberGenerator = Mockito.mock(CardNumberGenerator.class);
        cardEncryptionService = Mockito.mock(CardEncryptionService.class);
        securityUtil = Mockito.mock(SecurityUtil.class);
        when(securityUtil.getCurrentUserId()).thenReturn(10L);
        when(securityUtil.isAdmin()).thenReturn(false);
        service = new CardServiceImpl(cardRepository, cardNumberGenerator, cardEncryptionService, securityUtil, historyRepository);
        card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusDays(10));
        User u = new User();
        u.setId(10L);
        card.setUser(u);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void block_active_ok() {
        Card result = service.blockCard(1L);
        assertEquals(CardStatus.BLOCKED, result.getStatus());
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, atLeastOnce()).save(captor.capture());
        verify(historyRepository, atLeastOnce()).save(any());
    }

    @Test
    void block_already_blocked_error() {
        card.setStatus(CardStatus.BLOCKED);
        assertThrows(IllegalStateException.class, () -> service.blockCard(1L));
    }

    @Test
    void unblock_admin_ok() {
        card.setStatus(CardStatus.BLOCKED);
        when(securityUtil.isAdmin()).thenReturn(true);
        Card unblocked = service.unblockCard(1L);
        assertEquals(CardStatus.ACTIVE, unblocked.getStatus());
        verify(historyRepository).save(any());
    }

    @Test
    void unblock_non_admin_forbidden() {
        card.setStatus(CardStatus.BLOCKED);
        when(securityUtil.isAdmin()).thenReturn(false);
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.unblockCard(1L));
    }

    @Test
    void unblock_expired_error() {
        card.setStatus(CardStatus.EXPIRED);
        when(securityUtil.isAdmin()).thenReturn(true);
        assertThrows(CardStatusException.class, () -> service.unblockCard(1L));
    }

    @Test
    void block_expired_error() {
        card.setStatus(CardStatus.EXPIRED);
        assertThrows(CardStatusException.class, () -> service.blockCard(1L));
    }
}

