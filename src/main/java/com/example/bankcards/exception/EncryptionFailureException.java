package com.example.bankcards.exception;

public class EncryptionFailureException extends RuntimeException {
    public EncryptionFailureException(String message) {
        super(message);
    }
    public EncryptionFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

