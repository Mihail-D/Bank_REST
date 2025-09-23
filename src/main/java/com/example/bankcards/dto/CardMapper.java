package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardEncryptionService;

public class CardMapper {
    private final CardEncryptionService encryptionService;

    public CardMapper(CardEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public CardDto toDto(Card card) {
        String decrypted = encryptionService.decrypt(card.getEncryptedNumber());
        String masked = encryptionService.mask(decrypted);
        return new CardDto(
            card.getId(),
            masked,
            card.getStatus(),
            card.getExpirationDate()
        );
    }
}
