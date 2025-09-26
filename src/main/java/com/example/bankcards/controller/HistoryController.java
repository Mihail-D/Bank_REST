package com.example.bankcards.controller;

import com.example.bankcards.dto.HistoryFilterDto;
import com.example.bankcards.entity.History;
import com.example.bankcards.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import com.example.bankcards.exception.NotFoundException;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "История действий")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(ref = "Unauthorized"),
        @ApiResponse(ref = "Forbidden"),
        @ApiResponse(ref = "InternalServerError")
})
public class HistoryController {
    private final HistoryService historyService;
    private final PermissionService permissionService; // оставляем для совместимости
    private final SecurityUtil securityUtil; // новый компонент
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @Operation(summary = "Фильтрация истории", description = "Фильтры по пользователю, карте, переводу и интервалу времени")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = History.class)))
    })
    @GetMapping
    public Page<History> filterHistory(
            @Parameter(description = "ID пользователя", example = "1") @RequestParam(required = false) Long userId,
            @Parameter(description = "ID карты", example = "1001") @RequestParam(required = false) Long cardId,
            @Parameter(description = "ID перевода", example = "42") @RequestParam(required = false) Long transferId,
            @Parameter(description = "Тип события", example = "CARD_BLOCKED") @RequestParam(required = false) String eventType,
            @Parameter(description = "Дата с", example = "2025-09-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Дата по", example = "2025-09-30T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @PageableDefault Pageable pageable
    ) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        Long effectiveUserId = isAdmin ? userId : current; // USER всегда ограничен своим userId

        HistoryFilterDto filter = new HistoryFilterDto();
        filter.setUserId(effectiveUserId);
        filter.setCardId(cardId);
        filter.setTransferId(transferId);
        filter.setEventType(eventType);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);

        return historyService.findByFilter(filter, pageable);
    }

    @Operation(summary = "Получить запись истории по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = History.class))),
            @ApiResponse(ref = "NotFound")
    })
    @GetMapping("/{historyId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<History> getHistory(@Parameter(description = "ID записи", example = "10") @PathVariable Long historyId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String username = authentication.getName();
        if (!isAdmin && !permissionService.isHistoryOwner(historyId, username)) {
            log.debug("Forbidden access to history {} by user {}", historyId, username);
            throw new AccessDeniedException("Доступ запрещён к истории " + historyId);
        }
        History history = historyService.getHistoryById(historyId)
                .orElseThrow(() -> new NotFoundException("История с id " + historyId + " не найдена"));
        return ResponseEntity.ok(history);
    }
}
