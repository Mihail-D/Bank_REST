package com.example.bankcards.service;

import com.example.bankcards.entity.Transfer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransferService {
    Transfer createTransfer(Long fromCardId, Long toCardId, BigDecimal amount, Long userId);
    List<Transfer> getTransfersByUser(Long userId);
    List<Transfer> getTransfersByCard(Long cardId);
    List<Transfer> getTransfersByStatus(String status);
    Optional<Transfer> getTransferById(Long id);
}
