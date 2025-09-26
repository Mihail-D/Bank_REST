package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус банковской карты")
public enum CardStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED
}
