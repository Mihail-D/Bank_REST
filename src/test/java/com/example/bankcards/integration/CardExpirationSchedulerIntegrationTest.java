package com.example.bankcards.integration;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.scheduler.CardExpirationScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardExpirationSchedulerIntegrationTest {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private CardExpirationScheduler scheduler;

    private Card expiringCard;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setActive(true);
        u.setRole(Role.USER);
        u.setUsername("scheduler_user");
        u.setName("Scheduler User");
        u.setEmail("scheduler@example.com");
        u.setPassword("pwd");
        u = userRepository.save(u);

        expiringCard = new Card();
        expiringCard.setUser(u);
        expiringCard.setEncryptedNumber("sched_enc");
        expiringCard.setStatus(CardStatus.ACTIVE);
        expiringCard.setExpirationDate(LocalDate.now().minusDays(1)); // уже истекла
        expiringCard = cardRepository.save(expiringCard);
    }

    @Test
    void scheduler_marks_card_expired_and_history_written() {
        scheduler.triggerForTests();
        Card refreshed = cardRepository.findById(expiringCard.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(CardStatus.EXPIRED);
        assertThat(historyRepository.findAll())
                .anyMatch(h -> h.getCard() != null && h.getCard().getId().equals(expiringCard.getId()) && "CARD_EXPIRED".equals(h.getEventType()));
    }
}

