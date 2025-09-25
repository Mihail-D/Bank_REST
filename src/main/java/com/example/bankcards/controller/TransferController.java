package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.TransferFilter;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;
    private final PermissionService permissionService;
    private final SecurityUtil securityUtil;
    private final CardRepository cardRepository;
    private final TransferFilter transferFilter;

    @Autowired
    public TransferController(TransferService transferService, PermissionService permissionService, SecurityUtil securityUtil, CardRepository cardRepository, TransferFilter transferFilter) {
        this.transferService = transferService;
        this.permissionService = permissionService;
        this.securityUtil = securityUtil;
        this.cardRepository = cardRepository;
        this.transferFilter = transferFilter;
    }

    @PostMapping
    public ResponseEntity<?> createTransfer(@RequestParam Long fromCardId,
                                          @RequestParam Long toCardId,
                                          @RequestParam BigDecimal amount,
                                          @RequestParam Long userId) {
        try {
            Transfer transfer = transferService.createTransfer(fromCardId, toCardId, amount, userId);
            return ResponseEntity.ok(TransferMapper.toDto(transfer));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public List<TransferDto> getTransfersByUser(@PathVariable Long userId) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        if (!isAdmin && (current == null || !current.equals(userId))) {
            throw new AccessDeniedException("Доступ запрещён: нельзя просматривать переводы другого пользователя");
        }
        List<Transfer> all = transferService.getTransfersByUser(userId);
        // политика: всегда только исходящие пользователя (даже для админа сейчас)
        List<Transfer> outgoing = transferFilter.outgoingForUser(all, userId);
        return outgoing.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/card/{cardId}")
    public List<TransferDto> getTransfersByCard(@PathVariable Long cardId) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (!isAdmin) {
            if (current == null || !card.getUser().getId().equals(current)) {
                throw new AccessDeniedException("Доступ запрещён: нельзя просматривать переводы чужой карты");
            }
        }
        List<Transfer> all = transferService.getTransfersByCard(cardId);
        List<Transfer> outgoing = transferFilter.outgoingForCard(all, cardId);
        return outgoing.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<TransferDto> getTransfersByStatus(@PathVariable String status) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        List<Transfer> all = transferService.getTransfersByStatus(status);
        List<Transfer> visible = transferFilter.visibleForUserOrAdmin(all, current, isAdmin);
        // политика: даже для админа возвращаем только исходящие (visibleForUserOrAdmin сейчас не фильтрует админа) => дополнительно фильтруем
        if (isAdmin) {
            // админ смотрит только исходящие (владельцы sourceCard присутствуют) - политика текущего шага
            visible = visible.stream().filter(t -> t.getSourceCard() != null).collect(Collectors.toList());
        }
        return visible.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    // Ручная проверка прав вместо SpEL для согласованности с тестами
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferDto> getTransfer(@PathVariable Long transferId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = securityUtil.isAdmin();
        if (!isAdmin) {
            Long currentUserId = securityUtil.getCurrentUserId();
            if (currentUserId == null || !permissionService.isTransferOwner(transferId, currentUserId)) {
                log.debug("Forbidden access to transfer {} by user {}", transferId, authentication.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return transferService.getTransferById(transferId)
                .map(t -> ResponseEntity.ok(TransferMapper.toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }
}
