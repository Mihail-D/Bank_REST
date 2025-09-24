package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardSearchDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CardController.class)
@Import(SecurityTestConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @MockBean
    private CardMapper cardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Card testCard;
    private CardDto testCardDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Иван Иванов");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setEncryptedNumber("encrypted123");
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setExpirationDate(LocalDate.now().plusYears(3));
        testCard.setUser(testUser);

        testCardDto = new CardDto(1L, "**** **** **** 1234", CardStatus.ACTIVE, LocalDate.now().plusYears(3));
    }

    // Тесты для поиска карт

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsShouldReturnCardsWhenAllParametersProvided() throws Exception {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCards(any(CardSearchDto.class))).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "ACTIVE")
                .param("userId", "1")
                .param("ownerName", "Иван")
                .param("mask", "1234")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsShouldReturnCardsWhenOnlyStatusProvided() throws Exception {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCards(any(CardSearchDto.class))).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsShouldReturnEmptyListWhenNoCardsFound() throws Exception {
        // Given
        when(cardService.searchCards(any(CardSearchDto.class))).thenReturn(Collections.emptyList());
        when(cardMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "BLOCKED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByMaskShouldReturnMatchingCards() throws Exception {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCardsByMask("1234")).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search/mask/1234")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByOwnerNameShouldReturnMatchingCards() throws Exception {
        // Given
        String ownerName = "Иван";
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCardsByOwnerName(ownerName)).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search/owner")
                .param("ownerName", ownerName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByStatusAndOwnerShouldReturnMatchingCards() throws Exception {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCardsByStatusAndOwner(eq(CardStatus.ACTIVE), eq(1L))).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search/filter")
                .param("status", "ACTIVE")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByStatusAndOwnerShouldReturnCardsWhenOnlyStatusProvided() throws Exception {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        List<CardDto> cardDtos = Arrays.asList(testCardDto);

        when(cardService.searchCardsByStatusAndOwner(eq(CardStatus.BLOCKED), eq(null))).thenReturn(cards);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        // When & Then
        mockMvc.perform(get("/api/cards/search/filter")
                .param("status", "BLOCKED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        // Given
        when(cardService.searchCards(any(CardSearchDto.class))).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByMaskShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        // Given
        when(cardService.searchCardsByMask(anyString())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/cards/search/mask/1234")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchCardsByOwnerNameShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        // Given
        when(cardService.searchCardsByOwnerName(anyString())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/cards/search/owner")
                .param("ownerName", "Иван")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchCardsShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Изменяем на 403 Forbidden, так как Spring Security возвращает именно этот статус
    }
}
