package com.example.bankcards.scheduler;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.History;
import com.example.bankcards.entity.HistoryEventType;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CardExpirationScheduler {

    private final CardRepository cardRepository;
    private final HistoryRepository historyRepository;

    public CardExpirationScheduler(CardRepository cardRepository, HistoryRepository historyRepository) {
        this.cardRepository = cardRepository;
        this.historyRepository = historyRepository;
    }

    // Ежедневно в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireCards() {
        LocalDate today = LocalDate.now();
        List<Card> toExpire = cardRepository.findExpiredCardsByStatus(today, CardStatus.ACTIVE);
        for (Card card : toExpire) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            History h = new History();
            h.setEventType(HistoryEventType.CARD_EXPIRED);
            h.setEventDate(LocalDateTime.now());
            h.setDescription("Карта автоматически помечена как EXPIRED (дата истечения " + card.getExpirationDate() + ")");
            h.setCard(card);
            h.setUser(card.getUser());
            historyRepository.save(h);
        }
    }

    // Для интеграционных тестов
    public void triggerForTests() { expireCards(); }
}

