package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardSearchDto {

    private CardStatus status;
    private Long userId;
    private String ownerName;
    private String mask;
    private Boolean isExpired;
    private LocalDate createdAfter;
    private LocalDate createdBefore;

    // Конструктор для обратной совместимости
    public CardSearchDto(CardStatus status, Long userId, String ownerName, String mask) {
        this.status = status;
        this.userId = userId;
        this.ownerName = ownerName;
        this.mask = mask;
    }
}
