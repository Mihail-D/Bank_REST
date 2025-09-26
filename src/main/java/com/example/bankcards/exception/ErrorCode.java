package com.example.bankcards.exception;

public enum ErrorCode {
    // Общие
    BAD_REQUEST,
    VALIDATION_ERROR,
    ACCESS_DENIED,
    AUTHENTICATION_FAILED,
    NOT_FOUND,
    DATA_INTEGRITY_VIOLATION,
    INTERNAL_ERROR,

    // Доменные: пользователи/карты/переводы
    USER_NOT_FOUND,
    CARD_NOT_FOUND,
    TRANSFER_NOT_FOUND,

    // Бизнес-ошибки по картам и переводам
    INSUFFICIENT_FUNDS,
    CARD_BLOCKED,
    CARD_EXPIRED,
    CARD_INACTIVE,
    SAME_CARD_TRANSFER,

    // Криптография/тех ошибки
    ENCRYPTION_FAILURE
}

