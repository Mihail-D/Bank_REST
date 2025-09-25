package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.History;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.HistoryEventType;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityUtil;
import com.example.bankcards.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransferServiceImpl implements TransferService {
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    @Transactional
    public Transfer createTransfer(Long fromCardId, Long toCardId, BigDecimal amount, Long userId) {
        // Валидация: текущий пользователь должен совпадать с userId (если не админ)
        if (!securityUtil.isAdmin()) {
            Long current = securityUtil.getCurrentUserId();
            if (current == null || userId == null || !current.equals(userId)) {
                throw new AccessDeniedException("Доступ запрещён: нельзя инициировать перевод от имени другого пользователя");
            }
        }
        // edge case: перевод самому себе
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Нельзя переводить на ту же самую карту");
        }
        // edge case: отрицательная или нулевая сумма
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("Destination card not found"));

        // Проверка владения исходной картой
        if (!fromCard.getUser().getId().equals(userId) && !securityUtil.isAdmin()) {
            throw new SecurityException("User does not own the source card");
        }
        validateCardUsable(fromCard, "Исходная карта недоступна для перевода");
        validateCardUsable(toCard, "Целевая карта недоступна для перевода");

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer();
        transfer.setSourceCard(fromCard);
        transfer.setDestinationCard(toCard);
        transfer.setAmount(amount);
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("SUCCESS");
        Transfer savedTransfer = transferRepository.save(transfer);

        History history = new History();
        history.setEventType(HistoryEventType.TRANSFER);
        history.setEventDate(LocalDateTime.now());
        history.setDescription("Перевод с карты " + fromCardId + " на карту " + toCardId + " на сумму " + amount);
        history.setUser(fromCard.getUser());
        history.setCard(fromCard);
        history.setTransfer(savedTransfer);
        historyRepository.save(history);
        return savedTransfer;
    }

    private void validateCardUsable(Card card, String baseMsg) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardStatusException(baseMsg + ": статус BLOCKED");
        }
        if (card.getStatus() == CardStatus.EXPIRED || card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardStatusException(baseMsg + ": карта истекла");
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardStatusException(baseMsg + ": статус " + card.getStatus());
        }
    }

    @Override
    public List<Transfer> getTransfersByUser(Long userId) {
        return transferRepository.findByUserId(userId);
    }

    @Override
    public List<Transfer> getTransfersByCard(Long cardId) {
        return transferRepository.findByCardId(cardId);
    }

    @Override
    public List<Transfer> getTransfersByStatus(String status) {
        return transferRepository.findByStatus(status);
    }

    @Override
    public Optional<Transfer> getTransferById(Long id) {
        return transferRepository.findById(id);
    }
}
