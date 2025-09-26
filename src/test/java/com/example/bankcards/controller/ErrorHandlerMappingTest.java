package com.example.bankcards.controller;

import com.example.bankcards.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ErrorHandlerMappingTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test/errors")
    static class ThrowingController {
        @GetMapping("/not-found")
        public void notFound() { throw new CardNotFoundException("Card not found"); }

        @GetMapping("/business")
        public void business() { throw new InsufficientFundsException("Недостаточно средств на карте"); }

        @GetMapping("/validate")
        public String validate(@RequestParam @Positive Integer amount) { return "ok"; }
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void notFoundMappedTo404WithCode() throws Exception {
        mockMvc.perform(get("/test/errors/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.code").value("CARD_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    void businessConflictMappedTo409WithCode() throws Exception {
        mockMvc.perform(get("/test/errors/business").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    void validationErrorMappedTo400() throws Exception {
        mockMvc.perform(get("/test/errors/validate").param("amount", "-5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}

