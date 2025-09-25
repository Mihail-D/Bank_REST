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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.security.SecurityUtil;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;
    private final PermissionService permissionService; // оставляем для совместимости
    private final SecurityUtil securityUtil; // новый компонент
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @GetMapping
    public Page<History> filterHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) Long transferId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
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

    @GetMapping("/{historyId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<History> getHistory(@PathVariable Long historyId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String username = authentication.getName();
        if (!isAdmin && !permissionService.isHistoryOwner(historyId, username)) {
            log.debug("Forbidden access to history {} by user {}", historyId, username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return historyService.getHistoryById(historyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
