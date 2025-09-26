package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.PageableUtils;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.bankcards.exception.UserNotFoundException;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final PermissionService permissionService;
    private final SecurityUtil securityUtil; // новый компонент

    @Autowired
    public CardController(CardService cardService, UserService userService, CardMapper cardMapper, PermissionService permissionService, SecurityUtil securityUtil) {
        this.cardService = cardService;
        this.userService = userService;
        this.cardMapper = cardMapper;
        this.permissionService = permissionService;
        this.securityUtil = securityUtil;
    }

    // Создание новой карты
    @PostMapping("/user/{userId}")
    public ResponseEntity<CardDto> createCard(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Card card = cardService.createCard(user);
        CardDto cardDto = cardMapper.toDto(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }

    // Получение карты по ID с ручной проверкой прав вместо SpEL
    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long cardId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isAdmin = securityUtil.isAdmin();
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!isAdmin && (currentUserId == null || !permissionService.isCardOwner(cardId, currentUserId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return cardService.getCardById(cardId)
                .map(cardMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Получение всех карт пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long userId) {
        List<Card> cards = cardService.getCardsByUserId(userId);
        List<CardDto> cardDtos = cardMapper.toDtoList(cards);
        return ResponseEntity.ok(cardDtos);
    }

    // Получение активных карт пользователя
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<CardDto>> getActiveUserCards(@PathVariable Long userId) {
        List<Card> cards = cardService.getActiveCardsByUserId(userId);
        List<CardDto> cardDtos = cardMapper.toDtoList(cards);
        return ResponseEntity.ok(cardDtos);
    }

    // Получение карт по статусу
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CardDto>> getCardsByStatus(@PathVariable CardStatus status) {
        List<Card> cards = cardService.getCardsByStatus(status);
        List<CardDto> cardDtos = cardMapper.toDtoList(cards);
        return ResponseEntity.ok(cardDtos);
    }

    // Блокировка карты
    @PutMapping("/{cardId}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!permissionService.canModifyCard(cardId, securityUtil.getCurrentUserId(), securityUtil.isAdmin())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        Card blockedCard = cardService.blockCard(cardId);
        CardDto cardDto = cardMapper.toDto(blockedCard);
        return ResponseEntity.ok(cardDto);
    }

    // Разблокировка карты (только ADMIN)
    @PutMapping("/{cardId}/unblock")
    public ResponseEntity<CardDto> unblockCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!securityUtil.isAdmin()) {
            throw new AccessDeniedException("Доступ запрещён: только ADMIN может разблокировать карту");
        }
        Card unblocked = cardService.unblockCard(cardId);
        return ResponseEntity.ok(cardMapper.toDto(unblocked));
    }

    // Активация карты
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!permissionService.canModifyCard(cardId, securityUtil.getCurrentUserId(), securityUtil.isAdmin())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        Card activatedCard = cardService.activateCard(cardId);
        CardDto cardDto = cardMapper.toDto(activatedCard);
        return ResponseEntity.ok(cardDto);
    }

    // Деактивация карты
    @PutMapping("/{cardId}/deactivate")
    public ResponseEntity<CardDto> deactivateCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!permissionService.canModifyCard(cardId, securityUtil.getCurrentUserId(), securityUtil.isAdmin())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        Card deactivatedCard = cardService.deactivateCard(cardId);
        CardDto cardDto = cardMapper.toDto(deactivatedCard);
        return ResponseEntity.ok(cardDto);
    }

    // Перевыпуск карты
    @PostMapping("/{cardId}/renew")
    public ResponseEntity<CardDto> renewCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!permissionService.canModifyCard(cardId, securityUtil.getCurrentUserId(), securityUtil.isAdmin())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        Card renewedCard = cardService.renewCard(cardId);
        CardDto cardDto = cardMapper.toDto(renewedCard);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }

    // Удаление карты
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        var cardOpt = cardService.getCardById(cardId);
        if (cardOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!permissionService.canModifyCard(cardId, securityUtil.getCurrentUserId(), securityUtil.isAdmin())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    // Проверка статуса карты
    @GetMapping("/{cardId}/status")
    public ResponseEntity<CardStatusResponse> getCardStatus(@PathVariable Long cardId) {
        boolean isActive = cardService.isCardActive(cardId);
        boolean isExpired = cardService.isCardExpired(cardId);
        boolean isBlocked = cardService.isCardBlocked(cardId);
        boolean canPerformTransaction = cardService.canPerformTransaction(cardId);
        CardStatusResponse status = new CardStatusResponse(isActive, isExpired, isBlocked, canPerformTransaction);
        return ResponseEntity.ok(status);
    }

    // Поиск карт с комбинированными фильтрами
    @GetMapping("/search")
    public ResponseEntity<List<CardDto>> searchCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String mask) {
        try {
            boolean isAdmin = securityUtil.isAdmin();
            Long effectiveUserId = isAdmin ? userId : securityUtil.getCurrentUserId();
            CardSearchDto searchDto = new CardSearchDto(status, effectiveUserId, ownerName, mask);
            List<Card> cards = cardService.searchCards(searchDto);
            if (!isAdmin) {
                Long currentId = securityUtil.getCurrentUserId();
                cards = cards.stream().filter(c -> c.getUser() != null && c.getUser().getId() != null && c.getUser().getId().equals(currentId)).toList();
            }
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
            boolean isAdmin = securityUtil.isAdmin();
            List<Card> cards = cardService.searchCardsByMask(mask);
            if (!isAdmin) {
                Long currentId = securityUtil.getCurrentUserId();
                cards = cards.stream().filter(c -> c.getUser() != null && c.getUser().getId() != null && c.getUser().getId().equals(currentId)).toList();
            }
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
            boolean isAdmin = securityUtil.isAdmin();
            List<Card> cards = cardService.searchCardsByOwnerName(ownerName);
            if (!isAdmin) {
                Long currentId = securityUtil.getCurrentUserId();
                cards = cards.stream().filter(c -> c.getUser() != null && c.getUser().getId() != null && c.getUser().getId().equals(currentId)).toList();
            }
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
            boolean isAdmin = securityUtil.isAdmin();
            Long effectiveUserId = isAdmin ? userId : securityUtil.getCurrentUserId();
            List<Card> cards = cardService.searchCardsByStatusAndOwner(status, effectiveUserId);
            if (!isAdmin) {
                Long currentId = securityUtil.getCurrentUserId();
                cards = cards.stream().filter(c -> c.getUser() != null && c.getUser().getId() != null && c.getUser().getId().equals(currentId)).toList();
            }
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

    // Поиск карт с пагинацией (POST) — заменено на единый body DTO
    @PostMapping("/search/paginated")
    public ResponseEntity<PageResponseDto<CardDto>> searchCardsWithPagination(@Valid @RequestBody CardSearchPageRequest request) {
        Pageable pageable = PageableUtils.createPageable(request.getPage());
        Page<Card> cardPage = cardService.searchCardsWithPagination(request.getSearch(), pageable);
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
            boolean isAdmin = securityUtil.isAdmin();
            Long effectiveUserId = isAdmin ? userId : securityUtil.getCurrentUserId();
            CardSearchDto searchDto = new CardSearchDto();
            searchDto.setStatus(status);
            searchDto.setUserId(effectiveUserId);
            searchDto.setOwnerName(ownerName);
            searchDto.setIsExpired(isExpired);
            searchDto.setMask(mask);
            Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDirection);
            Page<Card> cardPage = cardService.searchCardsWithPagination(searchDto, pageable);
            List<Card> content = cardPage.getContent();
            if (!isAdmin) {
                Long currentId = securityUtil.getCurrentUserId();
                content = content.stream().filter(c -> c.getUser() != null && c.getUser().getId() != null && c.getUser().getId().equals(currentId)).toList();
            }
            List<CardDto> cardDtos = cardMapper.toDtoList(content);
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
