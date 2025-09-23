package com.example.bankcards.integration;

import com.example.bankcards.entity.*;
import com.example.bankcards.repository.*;
import com.example.bankcards.service.CardEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Import(CardEncryptionService.class)  // Импортируем CardEncryptionService в тестовый контекст
class EntityIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private CardEncryptionService cardEncryptionService;

    @Test
    void shouldCreateAndPersistUserWithCards() {
        User user = new User("John Doe", "johndoe", "john@example.com", "password123", Role.USER);
        // Инициализация коллекции карт
        user.setCards(new java.util.ArrayList<>());

        Card card1 = new Card();
        card1.setEncryptedNumber(cardEncryptionService.encrypt("1234567890123456"));
        card1.setStatus(CardStatus.ACTIVE);
        card1.setExpirationDate(LocalDate.now().plusYears(3));
        card1.setUser(user);
        user.getCards().add(card1);

        Card card2 = new Card();
        card2.setEncryptedNumber(cardEncryptionService.encrypt("6543210987654321"));
        card2.setStatus(CardStatus.BLOCKED);
        card2.setExpirationDate(LocalDate.now().plusYears(2));
        card2.setUser(user);
        user.getCards().add(card2);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");

        List<Card> userCards = cardRepository.findByUser(savedUser);
        assertThat(userCards).hasSize(2);
        assertThat(userCards).extracting(card -> cardEncryptionService.decrypt(card.getEncryptedNumber()))
                .containsExactlyInAnyOrder("1234567890123456", "6543210987654321");
    }

    @Test
    void shouldCreateTransferBetweenCards() {
        User sender = userRepository.save(new User("Sender", "sender", "sender@example.com", "pass", Role.USER));
        User receiver = userRepository.save(new User("Receiver", "receiver", "receiver@example.com", "pass", Role.USER));

        Card sourceCard = new Card();
        sourceCard.setEncryptedNumber(cardEncryptionService.encrypt("1111222233334444"));
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setExpirationDate(LocalDate.now().plusYears(3));
        sourceCard.setUser(sender);
        sourceCard = cardRepository.save(sourceCard);

        Card destinationCard = new Card();
        destinationCard.setEncryptedNumber(cardEncryptionService.encrypt("5555666677778888"));
        destinationCard.setStatus(CardStatus.ACTIVE);
        destinationCard.setExpirationDate(LocalDate.now().plusYears(2));
        destinationCard.setUser(receiver);
        destinationCard = cardRepository.save(destinationCard);

        Transfer transfer = new Transfer();
        transfer.setAmount(new BigDecimal("100.50"));
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setSourceCard(sourceCard);
        transfer.setDestinationCard(destinationCard);
        transfer.setStatus("COMPLETED");

        Transfer savedTransfer = transferRepository.save(transfer);

        assertThat(savedTransfer.getId()).isNotNull();
        assertThat(savedTransfer.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(savedTransfer.getSourceCard().getEncryptedNumber()).isEqualTo(cardEncryptionService.encrypt("1111222233334444"));
        assertThat(savedTransfer.getDestinationCard().getEncryptedNumber()).isEqualTo(cardEncryptionService.encrypt("5555666677778888"));
        assertThat(savedTransfer.getStatus()).isEqualTo("COMPLETED");

        List<Transfer> sourceTransfers = transferRepository.findBySourceCard(sourceCard);
        List<Transfer> destinationTransfers = transferRepository.findByDestinationCard(destinationCard);

        assertThat(sourceTransfers).hasSize(1);
        assertThat(destinationTransfers).hasSize(1);
        assertThat(sourceTransfers.get(0).getId()).isEqualTo(savedTransfer.getId());
        assertThat(destinationTransfers.get(0).getId()).isEqualTo(savedTransfer.getId());
    }

    @Test
    void shouldCreateHistoryEntryForUserCardAndTransfer() {
        User user = userRepository.save(new User("Test User", "testuser", "test@example.com", "pass", Role.USER));

        Card card = new Card();
        card.setEncryptedNumber(cardEncryptionService.encrypt("9999888877776666"));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setUser(user);
        cardRepository.save(card);

        Transfer transfer = new Transfer();
        transfer.setAmount(new BigDecimal("50.00"));
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setSourceCard(card);
        transfer.setDestinationCard(card);
        transfer.setStatus("PENDING");
        transfer = transferRepository.save(transfer);

        History history = new History();
        history.setEventType("TRANSFER_CREATED");
        history.setEventDate(LocalDateTime.now());
        history.setDescription("Transfer of 50.00 was created");
        history.setUser(user);
        history.setCard(card);
        history.setTransfer(transfer);

        History savedHistory = historyRepository.save(history);

        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getEventType()).isEqualTo("TRANSFER_CREATED");
        assertThat(savedHistory.getDescription()).isEqualTo("Transfer of 50.00 was created");
        assertThat(savedHistory.getUser().getUsername()).isEqualTo("testuser");
        assertThat(cardEncryptionService.decrypt(savedHistory.getCard().getEncryptedNumber())).isEqualTo("9999888877776666");
        assertThat(savedHistory.getTransfer().getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));

        List<History> userHistory = historyRepository.findByUser(user);
        List<History> cardHistory = historyRepository.findByCard(card);
        List<History> transferHistory = historyRepository.findByTransfer(transfer);

        assertThat(userHistory).hasSize(1);
        assertThat(cardHistory).hasSize(1);
        assertThat(transferHistory).hasSize(1);
    }

    @Test
    void shouldTestCardStatusEnum() {
        User user = userRepository.save(new User("Enum Test", "enumtest", "enum@example.com", "pass", Role.USER));

        Card activeCard = new Card();
        activeCard.setEncryptedNumber(cardEncryptionService.encrypt("1111000011110000"));
        activeCard.setStatus(CardStatus.ACTIVE);
        activeCard.setExpirationDate(LocalDate.now().plusYears(3));
        activeCard.setUser(user);

        Card blockedCard = new Card();
        blockedCard.setEncryptedNumber(cardEncryptionService.encrypt("2222000022220000"));
        blockedCard.setStatus(CardStatus.BLOCKED);
        blockedCard.setExpirationDate(LocalDate.now().plusYears(3));
        blockedCard.setUser(user);

        Card expiredCard = new Card();
        expiredCard.setEncryptedNumber(cardEncryptionService.encrypt("3333000033330000"));
        expiredCard.setStatus(CardStatus.EXPIRED);
        expiredCard.setExpirationDate(LocalDate.now().minusDays(1));
        expiredCard.setUser(user);

        cardRepository.save(activeCard);
        cardRepository.save(blockedCard);
        cardRepository.save(expiredCard);

        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);
        List<Card> blockedCards = cardRepository.findByStatus(CardStatus.BLOCKED);
        List<Card> expiredCards = cardRepository.findByStatus(CardStatus.EXPIRED);

        assertThat(activeCards).hasSize(1);
        assertThat(blockedCards).hasSize(1);
        assertThat(expiredCards).hasSize(1);

        assertThat(activeCards.get(0).getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(blockedCards.get(0).getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(expiredCards.get(0).getStatus()).isEqualTo(CardStatus.EXPIRED);
    }

    @Test
    void shouldTestRepositoryQueries() {
        User user = userRepository.save(new User("Query Test", "querytest", "query@example.com", "pass", Role.USER));

        Card card = new Card();
        card.setEncryptedNumber(cardEncryptionService.encrypt("4444555566667777"));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setUser(user);
        card = cardRepository.save(card);

        Optional<User> foundByUsername = userRepository.findByUsername("querytest");
        Optional<User> foundByEmail = userRepository.findByEmail("query@example.com");
        Optional<Card> foundByNumber = cardRepository.findByEncryptedNumber(cardEncryptionService.encrypt("4444555566667777"));
        List<Card> foundByUserId = cardRepository.findByUserIdAndStatus(user.getId(), CardStatus.ACTIVE);

        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getName()).isEqualTo("Query Test");

        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getUsername()).isEqualTo("querytest");

        assertThat(foundByNumber).isPresent();
        assertThat(foundByNumber.get().getUser().getId()).isEqualTo(user.getId());

        assertThat(foundByUserId).hasSize(1);
        assertThat(foundByUserId.get(0).getEncryptedNumber()).isEqualTo(cardEncryptionService.encrypt("4444555566667777"));
    }
}
