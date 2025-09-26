package com.example.bankcards.exception;

public class CardInactiveException extends RuntimeException {
    public CardInactiveException(String message) {
        super(message);
    }
}

