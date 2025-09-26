package com.example.bankcards.exception;

public class SameCardTransferException extends RuntimeException {
    public SameCardTransferException(String message) {
        super(message);
    }
}

