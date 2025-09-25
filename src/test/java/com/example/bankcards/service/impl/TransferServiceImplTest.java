package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.History;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private SecurityUtil securityUtil;
    @InjectMocks
    private TransferServiceImpl transferService;

    private Card fromCard;
    private Card toCard;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        user = new User();
        user.setId(1L);
        fromCard = new Card();
        fromCard.setId(10L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("100.00"));
        toCard = new Card();
        toCard.setId(20L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void createTransfer_success() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(i -> i.getArgument(0));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Transfer transfer = transferService.createTransfer(10L, 20L, new BigDecimal("30.00"), 1L);
        assertEquals(new BigDecimal("70.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("80.00"), toCard.getBalance());
        assertEquals(new BigDecimal("30.00"), transfer.getAmount());
        assertEquals("SUCCESS", transfer.getStatus());
    }

    @Test
    void createTransfer_insufficientBalance() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalArgumentException.class, () ->
                transferService.createTransfer(10L, 20L, new BigDecimal("200.00"), 1L));
    }

    @Test
    void createTransfer_inactiveCard() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalStateException.class, () ->
                transferService.createTransfer(10L, 20L, new BigDecimal("10.00"), 1L));
    }

    @Test
    void createTransfer_wrongUser() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        fromCard.setUser(anotherUser);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));
        assertThrows(SecurityException.class, () ->
                transferService.createTransfer(10L, 20L, new BigDecimal("10.00"), 1L));
    }

    @Test
    void createTransfer_auditHistoryCreated() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(i -> {
            Transfer t = i.getArgument(0);
            t.setId(100L);
            return t;
        });
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));
        when(historyRepository.save(any(History.class))).thenAnswer(i -> i.getArgument(0));
        transferService.createTransfer(10L, 20L, new BigDecimal("10.00"), 1L);
        verify(historyRepository, times(1)).save(any(History.class));
    }

    @Test
    void createTransfer_sameCard() {
        assertThrows(IllegalArgumentException.class, () ->
                transferService.createTransfer(10L, 10L, new BigDecimal("10.00"), 1L));
    }

    @Test
    void createTransfer_negativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                transferService.createTransfer(10L, 20L, new BigDecimal("-5.00"), 1L));
        assertThrows(IllegalArgumentException.class, () ->
                transferService.createTransfer(10L, 20L, BigDecimal.ZERO, 1L));
    }
}
