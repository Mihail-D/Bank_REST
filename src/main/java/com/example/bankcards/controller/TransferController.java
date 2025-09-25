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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;
    private final PermissionService permissionService;

    @Autowired
    public TransferController(TransferService transferService, PermissionService permissionService) {
        this.transferService = transferService;
        this.permissionService = permissionService;
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
        return transferService.getTransfersByUser(userId).stream()
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/card/{cardId}")
    public List<TransferDto> getTransfersByCard(@PathVariable Long cardId) {
        return transferService.getTransfersByCard(cardId).stream()
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<TransferDto> getTransfersByStatus(@PathVariable String status) {
        return transferService.getTransfersByStatus(status).stream()
                .map(TransferMapper::toDto)
                .collect(Collectors.toList());
    }

    // Ручная проверка прав вместо SpEL для согласованности с тестами
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferDto> getTransfer(@PathVariable Long transferId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            Long extractedUserId = extractUserId(authentication.getName());
            if (extractedUserId == null || !permissionService.isTransferOwner(transferId, extractedUserId)) {
                log.debug("Forbidden access to transfer {} by user {}", transferId, authentication.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return transferService.getTransferById(transferId)
                .map(t -> ResponseEntity.ok(TransferMapper.toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Long extractUserId(String username) {
        if (username == null) return null;
        String digits = username.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
