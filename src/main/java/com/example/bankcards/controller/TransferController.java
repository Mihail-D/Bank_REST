package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import com.example.bankcards.exception.CardNotFoundException;

// OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transfers")
@Validated
@Tag(name = "Transfers", description = "Операции переводов между картами")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(ref = "Unauthorized"),
        @ApiResponse(ref = "Forbidden"),
        @ApiResponse(ref = "InternalServerError")
})
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

    @Operation(summary = "Создать перевод между картами",
            description = "Создаёт перевод между своими картами. Пользователь не может переводить от имени другого.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferDto.class),
                            examples = @ExampleObject(value = "{\n  \"id\": 42,\n  \"fromCardId\": 1001,\n  \"toCardId\": 1002,\n  \"amount\": 1500.00,\n  \"transferDate\": \"2025-09-26T12:34:56\",\n  \"status\": \"SUCCESS\"\n}"))) ,
            @ApiResponse(ref = "BadRequest"),
            @ApiResponse(ref = "NotFound"),
            @ApiResponse(ref = "UnprocessableEntity"),
            @ApiResponse(ref = "Conflict")
    })
    @PostMapping
    public ResponseEntity<?> createTransfer(
            @Parameter(description = "ID исходной карты", example = "1001") @RequestParam @NotNull Long fromCardId,
            @Parameter(description = "ID целевой карты", example = "1002") @RequestParam @NotNull Long toCardId,
            @Parameter(description = "Сумма перевода", example = "1500.00") @RequestParam @NotNull BigDecimal amount,
            @Parameter(description = "ID текущего пользователя", example = "1") @RequestParam @NotNull Long userId) {
        Transfer transfer = transferService.createTransfer(fromCardId, toCardId, amount, userId);
        return ResponseEntity.ok(TransferMapper.toDto(transfer));
    }

    @Operation(summary = "Исходящие переводы пользователя", description = "Возвращает исходящие переводы указанного пользователя (сам пользователь или админ)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список переводов",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferDto.class)))
    })
    @GetMapping("/user/{userId}")
    public List<TransferDto> getTransfersByUser(@Parameter(description = "ID пользователя", example = "1") @PathVariable Long userId) {
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

    @Operation(summary = "Исходящие переводы по карте", description = "Возвращает исходящие переводы по указанной карте")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список переводов",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(ref = "NotFound")
    })
    @GetMapping("/card/{cardId}")
    public List<TransferDto> getTransfersByCard(@Parameter(description = "ID карты", example = "1001") @PathVariable Long cardId) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (!isAdmin) {
            if (current == null || !card.getUser().getId().equals(current)) {
                throw new AccessDeniedException("Доступ запрещён: нельзя просматривать переводы чужой карты");
            }
        }
        List<Transfer> all = transferService.getTransfersByCard(cardId);
        List<Transfer> outgoing = transferFilter.outgoingForCard(all, cardId);
        return outgoing.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "Переводы по статусу", description = "Возвращает переводы по статусу. Для админа отображаются только исходящие.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список переводов",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferDto.class)))
    })
    @GetMapping("/status/{status}")
    public List<TransferDto> getTransfersByStatus(@Parameter(description = "Статус перевода", example = "SUCCESS") @PathVariable String status) {
        boolean isAdmin = securityUtil.isAdmin();
        Long current = securityUtil.getCurrentUserId();
        List<Transfer> all = transferService.getTransfersByStatus(status);
        List<Transfer> visible = transferFilter.visibleForUserOrAdmin(all, current, isAdmin);
        // политика: даже для админа возвращаем только исходящие
        if (isAdmin) {
            visible = visible.stream().filter(t -> t.getSourceCard() != null).collect(Collectors.toList());
        }
        return visible.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "Получить перевод по id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Перевод найден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = TransferDto.class))),
        @ApiResponse(ref = "NotFound")
    })
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferDto> getTransfer(@Parameter(description = "ID перевода", example = "42") @PathVariable Long transferId, Authentication authentication) {
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
