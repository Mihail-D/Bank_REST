package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CardDto {
    private final Long id;
    private final String maskedNumber;
    private final CardStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate expirationDate;

    public CardDto(Long id, String maskedNumber, CardStatus status, LocalDate expirationDate) {
        this.id = id;
        this.maskedNumber = maskedNumber;
        this.status = status;
        this.expirationDate = expirationDate;
    }
}
