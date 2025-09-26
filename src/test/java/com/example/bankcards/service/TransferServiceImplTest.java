package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityUtil;
import com.example.bankcards.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock TransferRepository transferRepository;
    @Mock CardRepository cardRepository;
    @Mock HistoryRepository historyRepository;
    @Mock SecurityUtil securityUtil;

    @InjectMocks TransferServiceImpl service;

    private Card activeCard(long id, long userId, BigDecimal balance) {
        Card c = new Card();
        c.setId(id);
        c.setStatus(CardStatus.ACTIVE);
        c.setExpirationDate(LocalDate.now().plusDays(30));
        com.example.bankcards.entity.User u = new com.example.bankcards.entity.User();
        u.setId(userId);
        c.setUser(u);
        c.setBalance(balance);
        return c;
    }

    @BeforeEach
    void setupSecurity() {
        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    void createTransfer_throwsInsufficientFunds_whenBalanceTooLow() {
        Long fromId = 10L;
        Long toId = 20L;
        Long userId = 1L;
        Card from = activeCard(fromId, userId, new BigDecimal("50.00"));
        Card to = activeCard(toId, 2L, new BigDecimal("10.00"));
        when(cardRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(to));

        assertThrows(InsufficientFundsException.class, () ->
                service.createTransfer(fromId, toId, new BigDecimal("100.00"), userId)
        );
    }
}

