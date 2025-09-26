package com.example.bankcards.exception;

import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Единый формат ошибки API")
public record ErrorResponse(
        @Schema(description = "Метка времени возникновения ошибки", example = "2025-09-26T10:15:30Z", format = "date-time")
        Instant timestamp,
        @Schema(description = "HTTP статус", example = "400")
        int status,
        @Schema(description = "Краткое наименование статуса", example = "BAD_REQUEST")
        String error,
        @Schema(description = "Код ошибки доменной модели", example = "VALIDATION_ERROR")
        ErrorCode code,
        @Schema(description = "Человекочитаемое сообщение об ошибке", example = "Validation failed")
        String message,
        @Schema(description = "Путь запроса", example = "/api/cards/search")
        String path,
        @Schema(description = "Трассировочный идентификатор запроса", example = "e7d58f2b-f6a6-4a45-8e95-b3a0e7f2f1a9")
        String traceId,
        @Schema(description = "Ошибки валидации по полям (если применимо)")
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
