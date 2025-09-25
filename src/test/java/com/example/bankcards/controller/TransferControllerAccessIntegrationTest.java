package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransferControllerAccessIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private JwtService jwtService;

    private User user1;
    private User user2;
    private User admin;
    private Card user1CardA;
    private Card user1CardB;
    private Card user2CardA;
    private String tokenUser1;
    private String tokenAdmin;

    @BeforeEach
    void init() {
        transferRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();

        user1 = buildUser("u1","u1@mail.com", Role.USER);
        user2 = buildUser("u2","u2@mail.com", Role.USER);
        admin = buildUser("adminuser","admin@mail.com", Role.ADMIN);
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        admin = userRepository.save(admin);

        user1CardA = saveCard(user1);
        user1CardB = saveCard(user1);
        user2CardA = saveCard(user2);

        // transfers owned by user1 (source = user1CardA)
        saveTransfer(user1CardA, user1CardB, new BigDecimal("10.00"));
        saveTransfer(user1CardA, user2CardA, new BigDecimal("15.00"));
        // transfers owned by user2 (source = user2CardA)
        saveTransfer(user2CardA, user1CardA, new BigDecimal("5.00"));

        tokenUser1 = jwtService.generateToken(user1);
        tokenAdmin = jwtService.generateToken(admin);
    }

    @Test
    void userCanGetOwnUserTransfers_onlyHis() throws Exception {
        mockMvc.perform(get("/api/transfers/user/" + user1.getId())
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // user1 owns 2 source transfers
    }

    @Test
    void userCannotGetOtherUserTransfers() throws Exception {
        mockMvc.perform(get("/api/transfers/user/" + user2.getId())
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSeesAllTransfersForUser() throws Exception {
        mockMvc.perform(get("/api/transfers/user/" + user1.getId())
                .header("Authorization", "Bearer " + tokenAdmin)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void userGetsOwnCardTransfers_onlyOwn() throws Exception {
        mockMvc.perform(get("/api/transfers/card/" + user1CardA.getId())
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // two transfers with source user1CardA
    }

    @Test
    void userForbiddenOnForeignCardTransfers() throws Exception {
        mockMvc.perform(get("/api/transfers/card/" + user2CardA.getId())
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusEndpointFiltersForUser() throws Exception {
        mockMvc.perform(get("/api/transfers/status/SUCCESS")
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // only user1-owned transfers
    }

    @Test
    void statusEndpointShowsAllForAdmin() throws Exception {
        mockMvc.perform(get("/api/transfers/status/SUCCESS")
                .header("Authorization", "Bearer " + tokenAdmin)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // all transfers
    }

    private User buildUser(String username, String email, Role role) {
        User u = new User();
        u.setName(username);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("pass");
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private Card saveCard(User owner) {
        Card c = new Card();
        c.setEncryptedNumber("enc" + System.nanoTime());
        c.setStatus(CardStatus.ACTIVE);
        c.setExpirationDate(LocalDate.now().plusYears(1));
        c.setUser(owner);
        return cardRepository.save(c);
    }

    private Transfer saveTransfer(Card source, Card dest, BigDecimal amount) {
        Transfer t = new Transfer();
        t.setSourceCard(source);
        t.setDestinationCard(dest);
        t.setAmount(amount);
        t.setTransferDate(LocalDateTime.now());
        t.setStatus("SUCCESS");
        return transferRepository.save(t);
    }
}

