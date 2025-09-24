package com.example.bankcards.service;

import com.example.bankcards.dto.CardSearchDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;

import java.util.List;
import java.util.Optional;

public interface CardService {
    Card createCard(User user);

    // CRUD операции
    Optional<Card> getCardById(Long id);
    List<Card> getCardsByUser(User user);
    List<Card> getCardsByUserId(Long userId);
    List<Card> getCardsByStatus(CardStatus status);
    List<Card> getActiveCardsByUserId(Long userId);
    Optional<Card> getCardByMaskedNumber(String maskedNumber);

    // Методы поиска
    List<Card> searchCards(CardSearchDto searchDto);
    List<Card> searchCardsByMask(String mask);
    List<Card> searchCardsByOwnerName(String ownerName);
    List<Card> searchCardsByStatusAndOwner(CardStatus status, Long userId);

    // Операции управления картой
    Card blockCard(Long cardId);
    Card activateCard(Long cardId);
    Card deactivateCard(Long cardId);
    void deleteCard(Long cardId);

    // Проверки статуса
    boolean isCardActive(Long cardId);
    boolean isCardExpired(Long cardId);
    boolean isCardBlocked(Long cardId);

    // Бизнес-логика
    boolean canPerformTransaction(Long cardId);
    Card renewCard(Long cardId);
}
