# Формат ошибок API

Единый формат ошибки описан схемой `ErrorResponse` и применяется во всех типовых кейсах.

Структура:
- timestamp: ISO-8601 дата-время
- status: HTTP статус (число)
- error: текст статуса (например, BAD_REQUEST)
- code: доменный ErrorCode
- message: человекочитаемое сообщение
- path: путь запроса
- traceId: трассировочный идентификатор запроса
- validationErrors: карта ошибок валидации (поле -> сообщение), если применимо

## Матрица исключений → HTTP статус → ErrorCode

| Исключение | HTTP | error | ErrorCode |
|---|---:|---|---|
| MethodArgumentNotValidException | 400 | BAD_REQUEST | VALIDATION_ERROR |
| HandlerMethodValidationException | 400 | BAD_REQUEST | VALIDATION_ERROR |
| ConstraintViolationException | 400 | BAD_REQUEST | VALIDATION_ERROR |
| IllegalArgumentException | 400 | BAD_REQUEST | BAD_REQUEST |
| MethodArgumentTypeMismatchException | 400 | BAD_REQUEST | BAD_REQUEST |
| HttpMessageConversionException | 400 | BAD_REQUEST | BAD_REQUEST |
| RuntimeException | 400 | BAD_REQUEST | BAD_REQUEST |
| NotFoundException | 404 | NOT_FOUND | NOT_FOUND (или уточнённый) |
| UserNotFoundException | 404 | NOT_FOUND | USER_NOT_FOUND |
| CardNotFoundException | 404 | NOT_FOUND | CARD_NOT_FOUND |
| TransferNotFoundException | 404 | NOT_FOUND | TRANSFER_NOT_FOUND |
| CardExpiredException | 422 | UNPROCESSABLE_ENTITY | CARD_EXPIRED |
| CardInactiveException | 422 | UNPROCESSABLE_ENTITY | CARD_INACTIVE |
| CardStatusException | 422 | UNPROCESSABLE_ENTITY | BAD_REQUEST |
| InsufficientFundsException | 409 | CONFLICT | INSUFFICIENT_FUNDS |
| CardBlockedException | 409 | CONFLICT | CARD_BLOCKED |
| SameCardTransferException | 409 | CONFLICT | SAME_CARD_TRANSFER |
| DataIntegrityViolationException | 409 | CONFLICT | DATA_INTEGRITY_VIOLATION |
| AccessDeniedException | 403 | FORBIDDEN | ACCESS_DENIED |
| AuthenticationException | 401 | UNAUTHORIZED | AUTHENTICATION_FAILED |
| EncryptionFailureException | 500 | INTERNAL_SERVER_ERROR | ENCRYPTION_FAILURE |
| Exception (прочее) | 500 | INTERNAL_SERVER_ERROR | INTERNAL_ERROR |

Примечание: матрица соответствует текущей реализации `ErrorHandler`.

## Аутентификация и доступ

- Большинство эндпоинтов защищены JWT Bearer. Добавляйте заголовок:
  - `Authorization: Bearer <JWT>`
- Публичные эндпоинты: `/auth/register`, `/auth/login`.
- Типовые ответы безопасности:
  - `401 UNAUTHORIZED` (AUTHENTICATION_FAILED) — не передан или недействителен токен.
  - `403 FORBIDDEN` (ACCESS_DENIED) — нет прав на ресурс/операцию.

Примеры и эталонные ответы 400/401/403/404/409/422/500 вынесены в `components.responses` и доступны в Swagger UI.

## Примеры

### 400 BAD_REQUEST (валидация параметров)
```json
{
  "timestamp": "2025-09-26T10:15:30Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/cards/search",
  "traceId": "e7d58f2b-f6a6-4a45-8e95-b3a0e7f2f1a9",
  "validationErrors": {
    "status": "must not be null"
  }
}
```

### 404 NOT_FOUND (карта не найдена)
```json
{
  "timestamp": "2025-09-26T10:15:30Z",
  "status": 404,
  "error": "NOT_FOUND",
  "code": "CARD_NOT_FOUND",
  "message": "Card not found",
  "path": "/api/cards/123",
  "traceId": "d3ea04fe-25d8-4d35-a0f7-c50fd08c3b96",
  "validationErrors": null
}
```

### 409 CONFLICT (недостаточно средств)
```json
{
  "timestamp": "2025-09-26T10:15:30Z",
  "status": 409,
  "error": "CONFLICT",
  "code": "INSUFFICIENT_FUNDS",
  "message": "Недостаточно средств на карте",
  "path": "/api/transfers",
  "traceId": "0b8c0b65-1a6c-4ff7-b7e3-2cde1cf3b2a9",
  "validationErrors": null
}
```

### 422 UNPROCESSABLE_ENTITY (карта просрочена)
```json
{
  "timestamp": "2025-09-26T10:15:30Z",
  "status": 422,
  "error": "UNPROCESSABLE_ENTITY",
  "code": "CARD_EXPIRED",
  "message": "Срок действия карты истёк",
  "path": "/api/cards/1/status",
  "traceId": "5a9b3e7e-8d1a-4d66-9b8f-2f1a0c3d7e8b",
  "validationErrors": null
}
```

### 500 INTERNAL_SERVER_ERROR
```json
{
  "timestamp": "2025-09-26T10:15:30Z",
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "code": "INTERNAL_ERROR",
  "message": "Внутренняя ошибка сервера",
  "path": "/api/cards/search",
  "traceId": "a1b2c3d4-e5f6-7a89-b0c1-d2e3f4a5b6c7",
  "validationErrors": null
}
```

## Ссылки
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML (экспорт): `/v3/api-docs.yaml`
