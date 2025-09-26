package com.example.bankcards.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardSearchPageRequest {
    @Valid
    private CardSearchDto search;

    @Valid
    @NotNull(message = "Page request is required")
    private PageRequestDto page = new PageRequestDto();
}

