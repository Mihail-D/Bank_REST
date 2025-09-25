package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.History;
import com.example.bankcards.entity.Transfer;
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
        // Валидация прав пользователя, статуса карт, баланса
        if (!fromCard.getUser().getId().equals(userId) && !securityUtil.isAdmin()) {
            throw new SecurityException("User does not own the source card");
        }
        if (fromCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE ||
            toCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("Одна из карт неактивна");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте");
        }
        // Списание и зачисление
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        // Создание перевода
        Transfer transfer = new Transfer();
        transfer.setSourceCard(fromCard);
        transfer.setDestinationCard(toCard);
        transfer.setAmount(amount);
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("SUCCESS");
        Transfer savedTransfer = transferRepository.save(transfer);
        // Аудит перевода
        History history = new History();
        history.setEventType("TRANSFER");
        history.setEventDate(LocalDateTime.now());
        history.setDescription("Перевод с карты " + fromCardId + " на карту " + toCardId + " на сумму " + amount);
        history.setUser(fromCard.getUser());
        history.setCard(fromCard);
        history.setTransfer(savedTransfer);
        historyRepository.save(history);
        return savedTransfer;
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
