package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(CardEncryptionService.class)
class CardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardEncryptionService cardEncryptionService;

    private User testUser1;
    private User testUser2;
    private Card activeCard;
    private Card blockedCard;
    private Card expiredCard;

    @BeforeEach
    void setUp() {
        // Создаем тестовых пользователей
        testUser1 = new User("Test User 1", "testuser1", "test1@example.com", "password", "USER");
        testUser2 = new User("Test User 2", "testuser2", "test2@example.com", "password", "USER");

        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Создаем тестовые карты
        activeCard = createCard(testUser1, "1234567890123456", CardStatus.ACTIVE, LocalDate.now().plusYears(2));
        blockedCard = createCard(testUser1, "2345678901234567", CardStatus.BLOCKED, LocalDate.now().plusYears(1));
        expiredCard = createCard(testUser2, "3456789012345678", CardStatus.EXPIRED, LocalDate.now().minusMonths(1));

        entityManager.persistAndFlush(activeCard);
        entityManager.persistAndFlush(blockedCard);
        entityManager.persistAndFlush(expiredCard);
        entityManager.clear();
    }

    private Card createCard(User user, String cardNumber, CardStatus status, LocalDate expirationDate) {
        Card card = new Card();
        card.setUser(user);
        card.setEncryptedNumber(cardEncryptionService.encrypt(cardNumber));
        card.setStatus(status);
        card.setExpirationDate(expirationDate);
        return card;
    }

    @Test
    void shouldFindCardsByUser() {
        // When
        List<Card> user1Cards = cardRepository.findByUser(testUser1);
        List<Card> user2Cards = cardRepository.findByUser(testUser2);

        // Then
        assertThat(user1Cards).hasSize(2);
        assertThat(user2Cards).hasSize(1);

        assertThat(user1Cards).extracting(Card::getStatus)
                .containsExactlyInAnyOrder(CardStatus.ACTIVE, CardStatus.BLOCKED);
        assertThat(user2Cards.get(0).getStatus()).isEqualTo(CardStatus.EXPIRED);
    }

    @Test
    void shouldFindCardsByStatus() {
        // When
        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);
        List<Card> blockedCards = cardRepository.findByStatus(CardStatus.BLOCKED);
        List<Card> expiredCards = cardRepository.findByStatus(CardStatus.EXPIRED);

        // Then
        assertThat(activeCards).hasSize(1);
        assertThat(blockedCards).hasSize(1);
        assertThat(expiredCards).hasSize(1);
    }

    @Test
    void shouldFindCardsByUserIdAndStatus() {
        // When
        List<Card> user1ActiveCards = cardRepository.findByUserIdAndStatus(testUser1.getId(), CardStatus.ACTIVE);
        List<Card> user1BlockedCards = cardRepository.findByUserIdAndStatus(testUser1.getId(), CardStatus.BLOCKED);
        List<Card> user2ExpiredCards = cardRepository.findByUserIdAndStatus(testUser2.getId(), CardStatus.EXPIRED);

        // Then
        assertThat(user1ActiveCards).hasSize(1);
        assertThat(user1BlockedCards).hasSize(1);
        assertThat(user2ExpiredCards).hasSize(1);
    }

    @Test
    void shouldFindCardByEncryptedNumber() {
        // Given
        String encryptedNumber = activeCard.getEncryptedNumber();

        // When
        Optional<Card> foundCard = cardRepository.findByEncryptedNumber(encryptedNumber);

        // Then
        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getId()).isEqualTo(activeCard.getId());
        assertThat(foundCard.get().getUser().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void shouldReturnEmptyWhenCardNotFoundByEncryptedNumber() {
        // When
        Optional<Card> foundCard = cardRepository.findByEncryptedNumber("nonexistent");

        // Then
        assertThat(foundCard).isEmpty();
    }

    @Test
    void shouldFindCardsByUserId() {
        // When
        List<Card> user1Cards = cardRepository.findByUserId(testUser1.getId());
        List<Card> user2Cards = cardRepository.findByUserId(testUser2.getId());

        // Then
        assertThat(user1Cards).hasSize(2);
        assertThat(user2Cards).hasSize(1);
    }

    @Test
    void shouldFindExpiredCards() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        List<Card> expiredCards = cardRepository.findExpiredCards(today);

        // Then
        assertThat(expiredCards).hasSize(1);
        assertThat(expiredCards.get(0).getExpirationDate()).isBefore(today);
    }

    @Test
    void shouldFindExpiredCardsByStatus() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        List<Card> expiredActiveCards = cardRepository.findExpiredCardsByStatus(today, CardStatus.ACTIVE);
        List<Card> expiredExpiredCards = cardRepository.findExpiredCardsByStatus(today, CardStatus.EXPIRED);

        // Then
        assertThat(expiredActiveCards).isEmpty(); // нет просроченных активных карт
        assertThat(expiredExpiredCards).hasSize(1); // одна просроченная карта со статусом EXPIRED
    }

    @Test
    void shouldCountCardsByUserIdAndStatus() {
        // When
        long user1ActiveCount = cardRepository.countByUserIdAndStatus(testUser1.getId(), CardStatus.ACTIVE);
        long user1BlockedCount = cardRepository.countByUserIdAndStatus(testUser1.getId(), CardStatus.BLOCKED);
        long user2ExpiredCount = cardRepository.countByUserIdAndStatus(testUser2.getId(), CardStatus.EXPIRED);

        // Then
        assertThat(user1ActiveCount).isEqualTo(1);
        assertThat(user1BlockedCount).isEqualTo(1);
        assertThat(user2ExpiredCount).isEqualTo(1);
    }

    @Test
    void shouldFindCardsByUserIdAndStatusIn() {
        // Given
        List<CardStatus> statuses = List.of(CardStatus.ACTIVE, CardStatus.BLOCKED);

        // When
        List<Card> user1ActiveOrBlockedCards = cardRepository.findByUserIdAndStatusIn(testUser1.getId(), statuses);
        List<Card> user2ActiveOrBlockedCards = cardRepository.findByUserIdAndStatusIn(testUser2.getId(), statuses);

        // Then
        assertThat(user1ActiveOrBlockedCards).hasSize(2);
        assertThat(user2ActiveOrBlockedCards).isEmpty(); // у user2 только EXPIRED карта

        assertThat(user1ActiveOrBlockedCards).extracting(Card::getStatus)
                .containsExactlyInAnyOrder(CardStatus.ACTIVE, CardStatus.BLOCKED);
    }

    @Test
    void shouldHandleEdgeCasesForExpiredCards() {
        // Given - создаем карту, которая истекает сегодня
        Card todayExpiredCard = createCard(testUser1, "4567890123456789", CardStatus.ACTIVE, LocalDate.now());
        entityManager.persistAndFlush(todayExpiredCard);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When
        List<Card> expiredCards = cardRepository.findExpiredCards(tomorrow);

        // Then
        assertThat(expiredCards).hasSize(2); // старая просроченная + та что истекает сегодня
    }

    @Test
    void shouldFindNoExpiredCardsWhenDateInPast() {
        // Given
        LocalDate pastDate = LocalDate.now().minusYears(1);

        // When
        List<Card> expiredCards = cardRepository.findExpiredCards(pastDate);

        // Then
        assertThat(expiredCards).isEmpty();
    }

    @Test
    void shouldWorkWithEmptyDatabase() {
        // Given - очищаем базу данных
        cardRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // When
        List<Card> allCards = cardRepository.findAll();
        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);
        long activeCount = cardRepository.countByUserIdAndStatus(999L, CardStatus.ACTIVE);

        // Then
        assertThat(allCards).isEmpty();
        assertThat(activeCards).isEmpty();
        assertThat(activeCount).isZero();
    }
}
