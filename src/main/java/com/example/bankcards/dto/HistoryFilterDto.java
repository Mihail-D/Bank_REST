package com.example.bankcards.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoryFilterDto {
    private Long userId;
    private Long cardId;
    private Long transferId;
    private String eventType;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
}

