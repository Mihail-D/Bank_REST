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

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;
    private final PermissionService permissionService;
    private final SecurityUtil securityUtil;
    private final CardRepository cardRepository;

    @Autowired
    public TransferController(TransferService transferService, PermissionService permissionService, SecurityUtil securityUtil, CardRepository cardRepository) {
        this.transferService = transferService;
        this.permissionService = permissionService;
        this.securityUtil = securityUtil;
        this.cardRepository = cardRepository;
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
        return transferService.getTransfersByUser(userId).stream()
                .filter(t -> t.getSourceCard() != null && t.getSourceCard().getUser() != null
                        && t.getSourceCard().getUser().getId().equals(userId)) // оставляем только исходящие переводы владельца
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
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
        // загружаем все переводы и фильтруем только исходящие для этой карты
        List<Transfer> all = transferService.getTransfersByCard(cardId);
        return all.stream()
                .filter(t -> t.getSourceCard() != null && t.getSourceCard().getId().equals(cardId))
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<TransferDto> getTransfersByStatus(@PathVariable String status) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        return transferService.getTransfersByStatus(status).stream()
                .filter(t -> {
                    if (t.getSourceCard() == null || t.getSourceCard().getUser() == null) return false;
                    if (isAdmin) return true;
                    return t.getSourceCard().getUser().getId().equals(current);
                })
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
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
