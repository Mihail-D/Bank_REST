package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TransferControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    private User user;
    private Card fromCard;
    private Card toCard;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setActive(true);
        user.setRole(Role.USER);
        user = userRepository.save(user); // persist to get id
        jwtToken = jwtService.generateToken(user); // новый метод с userId claim
        fromCard = new Card();
        fromCard.setEncryptedNumber("enc1");
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpirationDate(java.time.LocalDate.now().plusYears(1));
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setUser(user);
        toCard = new Card();
        toCard.setEncryptedNumber("enc2");
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(java.time.LocalDate.now().plusYears(1));
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setUser(user);
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Test
    void transfer_success_and_audit() throws Exception {
        mockMvc.perform(post("/api/transfers")
                .param("fromCardId", fromCard.getId().toString())
                .param("toCardId", toCard.getId().toString())
                .param("amount", "30.00")
                .param("userId", user.getId().toString())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertThat(cardRepository.findById(fromCard.getId()).get().getBalance()).isEqualByComparingTo("70.00");
        assertThat(cardRepository.findById(toCard.getId()).get().getBalance()).isEqualByComparingTo("80.00");
        assertThat(transferRepository.findAll()).hasSize(1);
        assertThat(historyRepository.findAll()).anyMatch(h -> h.getEventType().equals("TRANSFER"));
    }

    @Test
    void transfer_sameCard_error() throws Exception {
        mockMvc.perform(post("/api/transfers")
                .param("fromCardId", fromCard.getId().toString())
                .param("toCardId", fromCard.getId().toString())
                .param("amount", "10.00")
                .param("userId", user.getId().toString())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Нельзя переводить на ту же самую карту")));
    }

    @Test
    void transfer_negativeAmount_error() throws Exception {
        mockMvc.perform(post("/api/transfers")
                .param("fromCardId", fromCard.getId().toString())
                .param("toCardId", toCard.getId().toString())
                .param("amount", "-10.00")
                .param("userId", user.getId().toString())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Сумма перевода должна быть положительной")));
    }

    @Test
    void transfer_inactiveCard_error() throws Exception {
        fromCard.setStatus(CardStatus.BLOCKED);
        cardRepository.save(fromCard);
        mockMvc.perform(post("/api/transfers")
                .param("fromCardId", fromCard.getId().toString())
                .param("toCardId", toCard.getId().toString())
                .param("amount", "10.00")
                .param("userId", user.getId().toString())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("статус BLOCKED")));
    }
}
