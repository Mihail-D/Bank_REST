package com.example.bankcards.exception;

/**
 * Исключение бизнес-логики для некорректных операций со статусом карты
 */
public class CardStatusException extends RuntimeException {
    public CardStatusException(String message) {
        super(message);
    }
}

