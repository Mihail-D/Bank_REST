package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardSearchDto {
    private CardStatus status;
    private Long userId;
    private String ownerName;
    private String mask; // последние 4 цифры карты

    public CardSearchDto() {}

    public CardSearchDto(CardStatus status, Long userId, String ownerName, String mask) {
        this.status = status;
        this.userId = userId;
        this.ownerName = ownerName;
        this.mask = mask;
    }
}
