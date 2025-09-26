package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Перевод между картами")
public class TransferDto {
    @Schema(description = "Идентификатор перевода", example = "42")
    private Long id;
    @Schema(description = "ID исходной карты", example = "1001")
    private Long fromCardId;
    @Schema(description = "ID целевой карты", example = "1002")
    private Long toCardId;
    @Schema(description = "Сумма перевода", example = "1500.00")
    private BigDecimal amount;
    @Schema(description = "Дата и время перевода", format = "date-time", example = "2025-09-26T12:34:56")
    private LocalDateTime transferDate;
    @Schema(description = "Статус перевода", example = "SUCCESS")
    private String status;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFromCardId() { return fromCardId; }
    public void setFromCardId(Long fromCardId) { this.fromCardId = fromCardId; }
    public Long getToCardId() { return toCardId; }
    public void setToCardId(Long toCardId) { this.toCardId = toCardId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
