# Матрица доступа (актуально после внедрённых доработок)

| Endpoint / Действие | Метод | Роль USER (владелец) | Роль USER (не владелец) | Роль ADMIN | Примечания / Условия |
|---------------------|-------|----------------------|-------------------------|------------|----------------------|
| /auth/register      | POST  | PUBLIC               | PUBLIC                  | PUBLIC     | Анонимно разрешено |
| /auth/login         | POST  | PUBLIC               | PUBLIC                  | PUBLIC     | Анонимно разрешено |
| /api/cards/{id}     | GET   | Разрешено (owner)    | 403                     | Разрешено  | Проверка PermissionService.isCardOwner + SecurityUtil |
| /api/cards/user/{userId} | GET | (нет фильтра сейчас) | (нет) | Разрешено | Требует доработки: ограничить по owner для USER |
| /api/cards/user/{userId}/active | GET | (нет фильтра) | (нет) | Разрешено | Как выше |
| /api/cards/status/{status} | GET | Разрешено (все карты) | Разрешено | Разрешено | Возможно стоит ограничить для USER только свои + статистика для ADMIN |
| /api/cards/{id}/block | PUT | Разрешено (owner) | 403 | Разрешено | Guard в CardServiceImpl.assertCanModify |
| /api/cards/{id}/activate | PUT | Разрешено (owner) | 403 | Разрешено | Guard |
| /api/cards/{id}/deactivate | PUT | Разрешено (owner) | 403 | Разрешено | Guard |
| /api/cards/{id}/renew | POST | Разрешено (owner) | 403 | Разрешено | Guard |
| /api/cards/{id} | DELETE | Разрешено (owner) | 403 | Разрешено | Guard |
| /api/cards/search* (все варианты) | GET/POST | Возвращает только свои (фильтрация в контроллере) | Н/Д | Полные данные | USER принудительно ограничен currentUserId |
| /api/cards/paginated* | GET | Сейчас без доп. фильтра | - | Полные данные | Нужно унифицировать фильтрацию как для search |
| /api/transfers (create) | POST | Только если userId == currentUserId и владелец fromCard | 403 | Разрешено | Guard в TransferServiceImpl + проверка владения картой |
| /api/transfers/{id} | GET | Разрешено (owner по sourceCard) | 403 | Разрешено | Проверка PermissionService.isTransferOwner в контроллере |
| /api/transfers/user/{userId} | GET | (нет фильтра) | (нет) | Разрешено | Нужно ограничить USER=owner |
| /api/transfers/card/{cardId} | GET | (нет фильтра) | (нет) | Разрешено | Аналогично |
| /api/transfers/status/{status} | GET | (нет фильтра) | (нет) | Разрешено | Возможно стоит ограничить до своих переводов |
| /api/history/{id} | GET | Разрешено (owner) | 403 | Разрешено | PermissionService.isHistoryOwner |
| /api/history (filter) | GET | (нет кастомной фильтрации) | (нет) | Разрешено | Нужно внедрить ограничение по userId для USER |

## Статус реализации
- JWT: добавлен claim `userId` (новый метод generateToken(User)). Legacy метод оставлен.
- SecurityUtil: единая точка получения userId, isAdmin, assertOwner.
- CardServiceImpl: guard'ы на модифицирующие операции, проверка создания карты.
- TransferServiceImpl: guard на соответствие userId текущему пользователю (если не ADMIN) и владение fromCard.
- Controllers (Card/Transfer/History): фильтрация поиска карт для USER, унификация 403 через исключения в части методов.
- ErrorHandler: единый формат JSON ошибки.

## Выявленные пробелы / TODO
1. Остальные листинговые эндпоинты (cards by user/status, transfers list, history filter) пока могут раскрывать чужие данные — требуется аналогичная фильтрация как в search.
2. Недостаточно @PreAuthorize на методах (можно заменить ручные проверки SpEL выражениями + сервисные guard'ы).
3. Нет unit-тестов на PermissionService (true/false ветви).
4. Нет расширенных тестов JwtAuthenticationFilter на userId claim и роль без префикса.
5. Не реализована централизованная ForbiddenOperationException (заведён класс, но не используется вместо AccessDeniedException повсеместно).
6. Отсутствует кэширование дешифровки номеров карт при поиске (производительность).
7. Методические тесты поиска: добавить негативный кейс, что USER не может получить карту другого пользователя через /status/{status} или /user/{userId}.
8. Возможен рефакторинг: вынести константы сообщений об ошибках в enum / класс.

## Предлагаемые следующие шаги (приоритет)
1. Ограничить выдачу всех list эндпоинтов (transfers, history, cards by user/status) — для USER автоматически применять currentUserId.
2. Внедрить `@PreAuthorize("hasRole('ADMIN') or #userId == @securityUtil.getCurrentUserId()")` (или аналог SpEL) поверх ручных проверок и постепенно удалить дубли.
3. Unit-тесты: PermissionService (isCardOwner, isTransferOwner, isHistoryOwner, canView/Modify), SecurityUtil edge cases.
4. Расширить JwtAuthenticationFilterTest на: отсутствует userId claim (fallback работает), неправильный формат userId, роль без префикса.
5. Внедрить ForbiddenOperationException в Card/Transfer сервисах вместо AccessDeniedException и адаптировать ErrorHandler.
6. Актуализировать OpenAPI (docs/openapi.yaml) с описанием 401/403 ответов и схемой ErrorResponse.

## Пример SpEL для будущих аннотаций
```
@PreAuthorize("hasRole('ADMIN') or @permissionService.isCardOwner(#cardId, @securityUtil.getCurrentUserId())")
```

## Формат ошибки (пример)
```
{
  "timestamp": "2025-09-25T10:15:30Z",
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Доступ запрещён",
  "path": "/api/cards/1"
}
```

---
Документ обновлять по мере внедрения шагов 1–6.

