package com.example.bankcards.controller;

import com.example.bankcards.entity.History;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.HistoryRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistoryControllerAccessIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private JwtService jwtService;

    private User user1;
    private User user2;
    private String tokenUser1;
    private String tokenAdmin;
    private User admin;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        userRepository.deleteAll();

        String suffix = String.valueOf(System.nanoTime());

        user1 = new User();
        user1.setName("User One");
        user1.setUsername("user_one_" + suffix);
        user1.setEmail("user1_" + suffix + "@mail.com");
        user1.setPassword("p1");
        user1.setRole(Role.USER);
        user1.setActive(true);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setUsername("user_two_" + suffix);
        user2.setEmail("user2_" + suffix + "@mail.com");
        user2.setPassword("p2");
        user2.setRole(Role.USER);
        user2.setActive(true);
        user2 = userRepository.save(user2);

        admin = new User();
        admin.setName("Admin");
        admin.setUsername("admin_user_" + suffix);
        admin.setEmail("admin_" + suffix + "@mail.com");
        admin.setPassword("pa");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin = userRepository.save(admin);

        for (int i = 0; i < 3; i++) {
            History h = new History();
            h.setEventType("EVT1");
            h.setEventDate(LocalDateTime.now().minusMinutes(i));
            h.setDescription("user1 event " + i);
            h.setUser(user1);
            historyRepository.save(h);
        }
        for (int i = 0; i < 2; i++) {
            History h = new History();
            h.setEventType("EVT2");
            h.setEventDate(LocalDateTime.now().minusMinutes(10 + i));
            h.setDescription("user2 event " + i);
            h.setUser(user2);
            historyRepository.save(h);
        }

        tokenUser1 = jwtService.generateToken(user1);
        tokenAdmin = jwtService.generateToken(admin);
    }

    @Test
    void userShouldSeeOnlyOwnHistory_whenNoUserIdParam() throws Exception {
        mockMvc.perform(get("/api/history")
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(jsonPath("$.content[*].user.id", everyItem(is(user1.getId().intValue()))));
    }

    @Test
    void userPassingOtherUserIdShouldStillSeeOnlyOwnHistory() throws Exception {
        mockMvc.perform(get("/api/history")
                .param("userId", user2.getId().toString())
                .header("Authorization", "Bearer " + tokenUser1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(jsonPath("$.content[*].user.id", everyItem(is(user1.getId().intValue()))));
    }

    @Test
    void adminShouldSeeFilteredByExplicitUserId_whenProvided() throws Exception {
        mockMvc.perform(get("/api/history")
                .param("userId", user2.getId().toString())
                .header("Authorization", "Bearer " + tokenAdmin)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.content[*].user.id", everyItem(is(user2.getId().intValue()))));
    }

    @Test
    void adminShouldSeeAllWhenNoUserIdParam() throws Exception {
        mockMvc.perform(get("/api/history")
                .header("Authorization", "Bearer " + tokenAdmin)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(5)));
    }
}
