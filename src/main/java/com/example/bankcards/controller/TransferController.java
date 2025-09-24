package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    @Autowired
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferDto> createTransfer(@RequestParam Long fromCardId,
                                                      @RequestParam Long toCardId,
                                                      @RequestParam BigDecimal amount,
                                                      @RequestParam Long userId) {
        Transfer transfer = transferService.createTransfer(fromCardId, toCardId, amount, userId);
        return ResponseEntity.ok(TransferMapper.toDto(transfer));
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
}

