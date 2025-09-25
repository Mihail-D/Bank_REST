package com.example.bankcards.controller;

import com.example.bankcards.entity.*;
import com.example.bankcards.repository.*;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TransferControllerCardStatusIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private CardRepository cardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;

    private User user;
    private Card expiredFrom;
    private Card activeTo;
    private String token;

    @BeforeEach
    void init() {
        user = new User();
        user.setUsername("expired_user");
        user.setName("Expired User");
        user.setEmail("exp@example.com");
        user.setPassword("pwd");
        user.setActive(true);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        token = jwtService.generateToken(user);

        expiredFrom = new Card();
        expiredFrom.setEncryptedNumber("expired_enc");
        expiredFrom.setStatus(CardStatus.ACTIVE); // статус ещё ACTIVE, но дата в прошлом => логика должна отказать
        expiredFrom.setExpirationDate(LocalDate.now().minusDays(2));
        expiredFrom.setBalance(new BigDecimal("100.00"));
        expiredFrom.setUser(user);
        expiredFrom = cardRepository.save(expiredFrom);

        activeTo = new Card();
        activeTo.setEncryptedNumber("active_enc");
        activeTo.setStatus(CardStatus.ACTIVE);
        activeTo.setExpirationDate(LocalDate.now().plusYears(1));
        activeTo.setBalance(new BigDecimal("10.00"));
        activeTo.setUser(user);
        activeTo = cardRepository.save(activeTo);
    }

    @Test
    void transfer_fromExpiredCard_422() throws Exception {
        mockMvc.perform(post("/api/transfers")
                .param("fromCardId", expiredFrom.getId().toString())
                .param("toCardId", activeTo.getId().toString())
                .param("amount", "5.00")
                .param("userId", user.getId().toString())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("карта истекла")));
    }
}

