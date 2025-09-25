package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardSearchDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User; // используем сущность
import com.example.bankcards.exception.ErrorHandler;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.security.SecurityConfig;
import org.springframework.context.annotation.*;
import org.springframework.security.test.context.support.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CardController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({CardControllerTest.Config.class, ErrorHandler.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CardService cardService;
    @Autowired
    private UserService userService;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private com.example.bankcards.security.PermissionService permissionService;

    @TestConfiguration
    static class Config {
        @Bean @Primary
        public CardService cardService() { return Mockito.mock(CardService.class); }
        @Bean @Primary
        public UserService userService() { return Mockito.mock(UserService.class); }
        @Bean @Primary
        public CardMapper cardMapper() { return Mockito.mock(CardMapper.class); }
        @Bean @Primary
        public com.example.bankcards.security.PermissionService permissionService() {
            com.example.bankcards.security.PermissionService mock = Mockito.mock(com.example.bankcards.security.PermissionService.class);
            Mockito.when(mock.isCardOwner(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
            return mock;
        }
        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            return Jackson2ObjectMapperBuilder.json()
                    .modules(new JavaTimeModule())
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build();
        }
        // Упрощённая security-конфигурация без JWT
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, exAuth) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, exDenied) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .anonymous(anon -> anon.disable())
                .httpBasic(httpBasic -> {});
            return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    org.springframework.security.core.userdetails.User.withUsername("user1").password("pass").roles("USER").build(),
                    org.springframework.security.core.userdetails.User.withUsername("admin").password("pass").roles("ADMIN").build()
            );
        }

        @Bean
        public PasswordEncoder passwordEncoder() { return NoOpPasswordEncoder.getInstance(); }
    }

    private Card testCard;
    private CardDto testCardDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        Mockito.reset(cardService, cardMapper, userService, permissionService);
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
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

        // When & Then
        mockMvc.perform(get("/api/cards/search")
                .param("status", "ACTIVE")
                .param("userId", "1")
                .param("ownerName", "Иван")
                .param("mask", "1234")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
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
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

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
        when(cardMapper.toDtoList(anyList())).thenReturn(Collections.emptyList());

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
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

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
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

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
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

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

        when(cardService.searchCardsByStatusAndOwner(eq(CardStatus.BLOCKED), isNull())).thenReturn(cards);
        when(cardMapper.toDtoList(anyList())).thenReturn(cardDtos);
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDto);

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getCard_shouldReturnOk_whenAdmin() throws Exception {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        User owner = new User();
        owner.setId(2L);
        card.setUser(owner);
        when(cardService.getCardById(cardId)).thenReturn(java.util.Optional.of(card));
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto(cardId, "**** **** **** 1234", CardStatus.ACTIVE, java.time.LocalDate.now().plusYears(3)));
        mockMvc.perform(get("/api/cards/" + cardId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getCard_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        Long cardId = 1L;
        Long ownerId = 2L;
        Card card = new Card();
        card.setId(cardId);
        User owner = new User();
        owner.setId(ownerId);
        card.setUser(owner);
        when(cardService.getCardById(cardId)).thenReturn(java.util.Optional.of(card));
        when(permissionService.isCardOwner(cardId, 1L)).thenReturn(false);
        mockMvc.perform(get("/api/cards/" + cardId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCard_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        Long cardId = 1L;
        mockMvc.perform(get("/api/cards/" + cardId))
                .andExpect(status().isUnauthorized());
    }
}
