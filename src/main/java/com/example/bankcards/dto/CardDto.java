package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Банковская карта (представление для клиента)")
@Getter
public class CardDto {
    @Schema(description = "Идентификатор карты", example = "123")
    private final Long id;
    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private final String maskedNumber;
    @Schema(description = "Статус карты")
    private final CardStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Срок действия (год-месяц-день)", example = "2027-09-01", format = "date")
    private final LocalDate expirationDate;

    public CardDto(Long id, String maskedNumber, CardStatus status, LocalDate expirationDate) {
        this.id = id;
        this.maskedNumber = maskedNumber;
        this.status = status;
        this.expirationDate = expirationDate;
    }
}
