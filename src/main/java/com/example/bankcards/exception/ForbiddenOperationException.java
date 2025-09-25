package com.example.bankcards.exception;

import org.springframework.security.access.AccessDeniedException;

public class ForbiddenOperationException extends AccessDeniedException {
    public ForbiddenOperationException(String msg) {
        super(msg);
    }
}

