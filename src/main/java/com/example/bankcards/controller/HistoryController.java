package com.example.bankcards.controller;

import com.example.bankcards.dto.HistoryFilterDto;
import com.example.bankcards.entity.History;
import com.example.bankcards.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

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
        HistoryFilterDto filter = new HistoryFilterDto();
        filter.setUserId(userId);
        filter.setCardId(cardId);
        filter.setTransferId(transferId);
        filter.setEventType(eventType);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        return historyService.findByFilter(filter, pageable);
    }
}

