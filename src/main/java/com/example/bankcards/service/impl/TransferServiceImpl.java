package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferServiceImpl implements TransferService {
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private CardRepository cardRepository;

    @Override
    @Transactional
    public Transfer createTransfer(Long fromCardId, Long toCardId, BigDecimal amount, Long userId) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("Destination card not found"));
        // Валидация прав пользователя, статуса карт, баланса
        if (!fromCard.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own the source card");
        }
        if (fromCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE ||
            toCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("One of the cards is not active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
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
        return transferRepository.save(transfer);
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
}

