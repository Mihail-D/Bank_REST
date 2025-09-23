package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardMapper {
    private final CardEncryptionService encryptionService;

    @Autowired
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

    public List<CardDto> toDtoList(List<Card> cards) {
        return cards.stream()
                .map(this::toDto)
                .toList();
    }
}
