package com.example.bankcards.exception;

import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        ErrorCode code,
        String message,
        String path,
        String traceId,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, ErrorCode code, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, code, message, path, generateTraceId(), null);
    }

    public static ErrorResponse validation(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, ErrorCode.VALIDATION_ERROR, message, path, generateTraceId(), fieldErrors);
    }

    public static String generateTraceId() {
        String tid = MDC.get("traceId");
        return tid != null ? tid : java.util.UUID.randomUUID().toString();
    }
}
