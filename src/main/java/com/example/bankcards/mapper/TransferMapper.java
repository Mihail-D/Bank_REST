package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Transfer;

public class TransferMapper {
    public static TransferDto toDto(Transfer transfer) {
        TransferDto dto = new TransferDto();
        dto.setId(transfer.getId());
        dto.setFromCardId(transfer.getSourceCard() != null ? transfer.getSourceCard().getId() : null);
        dto.setToCardId(transfer.getDestinationCard() != null ? transfer.getDestinationCard().getId() : null);
        dto.setAmount(transfer.getAmount());
        dto.setTransferDate(transfer.getTransferDate());
        dto.setStatus(transfer.getStatus());
        return dto;
    }
}

