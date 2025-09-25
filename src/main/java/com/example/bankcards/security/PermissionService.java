package com.example.bankcards.security;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.HistoryService;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("permissionService")
public class PermissionService {
    private final CardService cardService;
    private final TransferService transferService;
    private final HistoryService historyService;
    private final UserRepository userRepository;

    @Autowired
    public PermissionService(CardService cardService, TransferService transferService, HistoryService historyService, UserRepository userRepository) {
        this.cardService = cardService;
        this.transferService = transferService;
        this.historyService = historyService;
        this.userRepository = userRepository;
    }

    public boolean isCardOwner(Long cardId, Long userId) {
        return cardService.getCardById(cardId)
                .map(card -> card.getUser() != null && card.getUser().getId().equals(userId))
                .orElse(false);
    }

    public boolean isTransferOwner(Long transferId, Long userId) {
        return transferService.getTransferById(transferId)
                .map(transfer -> transfer.getSourceCard() != null &&
                        transfer.getSourceCard().getUser() != null &&
                        transfer.getSourceCard().getUser().getId().equals(userId))
                .orElse(false);
    }

    public boolean isHistoryOwner(Long historyId, String username) {
        return historyService.getHistoryById(historyId)
                .map(history -> history.getUser() != null &&
                        history.getUser().getUsername() != null &&
                        history.getUser().getUsername().equals(username))
                .orElse(false);
    }

    public boolean isCardOwnerUsername(Long cardId, String username) {
        if (username == null) return false;
        return cardService.getCardById(cardId)
                .map(card -> card.getUser() != null && username.equals(card.getUser().getUsername()))
                .orElse(false);
    }

    public boolean isTransferOwnerByUsername(Long transferId, String username) {
        if (username == null) return false;
        return transferService.getTransferById(transferId)
                .map(t -> t.getSourceCard() != null && t.getSourceCard().getUser() != null && username.equals(t.getSourceCard().getUser().getUsername()))
                .orElse(false);
    }

    public boolean isUserMatches(Long userId, String username) {
        if (username == null) return false;
        return userRepository.findById(userId)
                .map(u -> username.equals(u.getUsername()))
                .orElse(false);
    }

    public boolean canViewCard(Long cardId, Long userId, boolean isAdmin) {
        if (isAdmin) return true;
        return isCardOwner(cardId, userId);
    }

    public boolean canModifyCard(Long cardId, Long userId, boolean isAdmin) {
        if (isAdmin) return true;
        return isCardOwner(cardId, userId);
    }
}
