package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.PageableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;
    private final CardMapper cardMapper;

    @Autowired
    public CardController(CardService cardService, UserService userService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.userService = userService;
        this.cardMapper = cardMapper;
    }

    // Создание новой карты
    @PostMapping("/user/{userId}")
    public ResponseEntity<CardDto> createCard(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            Card card = cardService.createCard(user);
            CardDto cardDto = cardMapper.toDto(card);

            return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение карты по ID
    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId)
                .map(cardMapper::toDto)
                .map(cardDto -> ResponseEntity.ok(cardDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Получение всех карт пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long userId) {
        try {
            List<Card> cards = cardService.getCardsByUserId(userId);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение активных карт пользователя
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<CardDto>> getActiveUserCards(@PathVariable Long userId) {
        try {
            List<Card> cards = cardService.getActiveCardsByUserId(userId);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение карт по статусу
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CardDto>> getCardsByStatus(@PathVariable CardStatus status) {
        try {
            List<Card> cards = cardService.getCardsByStatus(status);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Блокировка карты
    @PutMapping("/{cardId}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        try {
            Card blockedCard = cardService.blockCard(cardId);
            CardDto cardDto = cardMapper.toDto(blockedCard);
            return ResponseEntity.ok(cardDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Активация карты
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        try {
            Card activatedCard = cardService.activateCard(cardId);
            CardDto cardDto = cardMapper.toDto(activatedCard);
            return ResponseEntity.ok(cardDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Деактивация карты
    @PutMapping("/{cardId}/deactivate")
    public ResponseEntity<CardDto> deactivateCard(@PathVariable Long cardId) {
        try {
            Card deactivatedCard = cardService.deactivateCard(cardId);
            CardDto cardDto = cardMapper.toDto(deactivatedCard);
            return ResponseEntity.ok(cardDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Перевыпуск карты
    @PostMapping("/{cardId}/renew")
    public ResponseEntity<CardDto> renewCard(@PathVariable Long cardId) {
        try {
            Card renewedCard = cardService.renewCard(cardId);
            CardDto cardDto = cardMapper.toDto(renewedCard);
            return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Удаление карты
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        try {
            cardService.deleteCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Проверка статуса карты
    @GetMapping("/{cardId}/status")
    public ResponseEntity<CardStatusResponse> getCardStatus(@PathVariable Long cardId) {
        try {
            boolean isActive = cardService.isCardActive(cardId);
            boolean isExpired = cardService.isCardExpired(cardId);
            boolean isBlocked = cardService.isCardBlocked(cardId);
            boolean canPerformTransaction = cardService.canPerformTransaction(cardId);

            CardStatusResponse status = new CardStatusResponse(isActive, isExpired, isBlocked, canPerformTransaction);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Поиск карт с комбинированными фильтрами
    @GetMapping("/search")
    public ResponseEntity<List<CardDto>> searchCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String mask) {
        try {
            CardSearchDto searchDto = new CardSearchDto(status, userId, ownerName, mask);
            List<Card> cards = cardService.searchCards(searchDto);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Поиск карт по маске номера
    @GetMapping("/search/mask/{mask}")
    public ResponseEntity<List<CardDto>> searchCardsByMask(@PathVariable String mask) {
        try {
            List<Card> cards = cardService.searchCardsByMask(mask);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Поиск карт по имени владельца
    @GetMapping("/search/owner")
    public ResponseEntity<List<CardDto>> searchCardsByOwnerName(@RequestParam String ownerName) {
        try {
            List<Card> cards = cardService.searchCardsByOwnerName(ownerName);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Поиск карт по статусу и владельцу
    @GetMapping("/search/filter")
    public ResponseEntity<List<CardDto>> searchCardsByStatusAndOwner(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long userId) {
        try {
            List<Card> cards = cardService.searchCardsByStatusAndOwner(status, userId);
            List<CardDto> cardDtos = cardMapper.toDtoList(cards);
            return ResponseEntity.ok(cardDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение всех карт с пагинацией
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> getAllCardsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDirection);
            Page<Card> cardPage = cardService.getAllCardsWithPagination(pageable);

            List<CardDto> cardDtos = cardMapper.toDtoList(cardPage.getContent());

            PageResponseDto<CardDto> response = PageResponseDto.of(
                cardDtos,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.isFirst(),
                cardPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение карт пользователя с пагинацией
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> getUserCardsWithPagination(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDirection);
            Page<Card> cardPage = cardService.getCardsByUserIdWithPagination(userId, pageable);

            List<CardDto> cardDtos = cardMapper.toDtoList(cardPage.getContent());

            PageResponseDto<CardDto> response = PageResponseDto.of(
                cardDtos,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.isFirst(),
                cardPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение карт по статусу с пагинацией
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> getCardsByStatusWithPagination(
            @PathVariable CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDirection);
            Page<Card> cardPage = cardService.getCardsByStatusWithPagination(status, pageable);

            List<CardDto> cardDtos = cardMapper.toDtoList(cardPage.getContent());

            PageResponseDto<CardDto> response = PageResponseDto.of(
                cardDtos,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.isFirst(),
                cardPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Поиск карт с пагинацией
    @PostMapping("/search/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> searchCardsWithPagination(
            @Valid @RequestBody CardSearchDto searchDto,
            @Valid @RequestBody PageRequestDto pageRequest) {

        try {
            Pageable pageable = PageableUtils.createPageable(pageRequest);
            Page<Card> cardPage = cardService.searchCardsWithPagination(searchDto, pageable);

            List<CardDto> cardDtos = cardMapper.toDtoList(cardPage.getContent());

            PageResponseDto<CardDto> response = PageResponseDto.of(
                cardDtos,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.isFirst(),
                cardPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Поиск карт с пагинацией через GET параметры
    @GetMapping("/search/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> searchCardsWithPaginationGet(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) Boolean isExpired,
            @RequestParam(required = false) String mask,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            CardSearchDto searchDto = new CardSearchDto();
            searchDto.setStatus(status);
            searchDto.setUserId(userId);
            searchDto.setOwnerName(ownerName);
            searchDto.setIsExpired(isExpired);
            searchDto.setMask(mask);

            Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDirection);
            Page<Card> cardPage = cardService.searchCardsWithPagination(searchDto, pageable);

            List<CardDto> cardDtos = cardMapper.toDtoList(cardPage.getContent());

            PageResponseDto<CardDto> response = PageResponseDto.of(
                cardDtos,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.isFirst(),
                cardPage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Вложенный класс для ответа со статусом карты
    public static class CardStatusResponse {
        private final boolean active;
        private final boolean expired;
        private final boolean blocked;
        private final boolean canPerformTransaction;

        public CardStatusResponse(boolean active, boolean expired, boolean blocked, boolean canPerformTransaction) {
            this.active = active;
            this.expired = expired;
            this.blocked = blocked;
            this.canPerformTransaction = canPerformTransaction;
        }

        public boolean isActive() { return active; }
        public boolean isExpired() { return expired; }
        public boolean isBlocked() { return blocked; }
        public boolean isCanPerformTransaction() { return canPerformTransaction; }
    }
}
