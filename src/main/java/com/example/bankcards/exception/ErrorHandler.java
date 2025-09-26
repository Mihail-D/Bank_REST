package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    // 400 — Неверные данные (валидация тела запроса)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        log.warn("Validation failed: {}", fieldErrors);
        ErrorResponse body = ErrorResponse.validation(400, "BAD_REQUEST", "Validation failed", req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 — Неверные данные (валидация параметров контроллера Spring 6: HandlerMethodValidationException)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getAllValidationResults().forEach(r -> {
            String param = r.getMethodParameter() != null ? r.getMethodParameter().getParameterName() : "param";
            String msg = r.getResolvableErrors().isEmpty() ? "Validation failure" : r.getResolvableErrors().get(0).getDefaultMessage();
            fieldErrors.put(param, msg);
        });
        log.warn("Handler method validation: {}", fieldErrors);
        ErrorResponse body = ErrorResponse.validation(400, "BAD_REQUEST", "Validation failed", req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 — Неверные данные (валидация параметров path/query)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            fieldErrors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        log.warn("Constraint violation: {}", fieldErrors);
        ErrorResponse body = ErrorResponse.validation(400, "BAD_REQUEST", "Constraint violation", req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 — Неверный формат параметра/запроса (+ RuntimeException из слоёв сервисов для единообразия на поисковых эндпоинтах)
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, HttpMessageConversionException.class, RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(400, "BAD_REQUEST", ErrorCode.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 404 — Не найдено
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ErrorCode code = ErrorCode.NOT_FOUND;
        if (ex instanceof UserNotFoundException) code = ErrorCode.USER_NOT_FOUND;
        if (ex instanceof CardNotFoundException) code = ErrorCode.CARD_NOT_FOUND;
        if (ex instanceof TransferNotFoundException) code = ErrorCode.TRANSFER_NOT_FOUND;
        log.warn("Not found: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(404, "NOT_FOUND", code, ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 422 — Невалидное состояние ресурса (например, статус карты)
    @ExceptionHandler({CardExpiredException.class, CardInactiveException.class, CardStatusException.class})
    public ResponseEntity<ErrorResponse> handleUnprocessable(Exception ex, HttpServletRequest req) {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        if (ex instanceof CardExpiredException) code = ErrorCode.CARD_EXPIRED;
        else if (ex instanceof CardInactiveException) code = ErrorCode.CARD_INACTIVE;
        log.warn("Unprocessable entity: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(422, "UNPROCESSABLE_ENTITY", code, ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // 409 — Конфликты бизнес-логики (остаток)
    @ExceptionHandler({InsufficientFundsException.class, CardBlockedException.class, SameCardTransferException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleBusinessConflict(Exception ex, HttpServletRequest req) {
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        if (ex instanceof InsufficientFundsException) code = ErrorCode.INSUFFICIENT_FUNDS;
        else if (ex instanceof CardBlockedException) code = ErrorCode.CARD_BLOCKED;
        else if (ex instanceof SameCardTransferException) code = ErrorCode.SAME_CARD_TRANSFER;
        else if (ex instanceof DataIntegrityViolationException) code = ErrorCode.DATA_INTEGRITY_VIOLATION;
        log.warn("Business conflict: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(409, "CONFLICT", code, ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 403 — Нет прав доступа
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(403, "FORBIDDEN", ErrorCode.ACCESS_DENIED, "Доступ запрещён", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 401 — Необходима аутентификация
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(401, "UNAUTHORIZED", ErrorCode.AUTHENTICATION_FAILED, "Необходима аутентификация", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 500 — Тех ошибки (например, шифрование)
    @ExceptionHandler(EncryptionFailureException.class)
    public ResponseEntity<ErrorResponse> handleEncryption(EncryptionFailureException ex, HttpServletRequest req) {
        log.error("Encryption failure", ex);
        ErrorResponse body = ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", ErrorCode.ENCRYPTION_FAILURE, "Ошибка шифрования", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 500 — Другое необработанное (попадут только checked-исключения и ошибки JVM)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        ErrorResponse body = ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", ErrorCode.INTERNAL_ERROR, "Внутренняя ошибка сервера", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
