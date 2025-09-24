package com.example.bankcards.controller;

import com.example.bankcards.entity.History;
import com.example.bankcards.repository.HistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "USER")
@SpringBootTest
@AutoConfigureMockMvc
class HistoryControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private HistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        History h1 = new History();
        h1.setEventType("TRANSFER");
        h1.setEventDate(LocalDateTime.now().minusDays(1));
        h1.setDescription("Test transfer");
        historyRepository.save(h1);
        History h2 = new History();
        h2.setEventType("BLOCK");
        h2.setEventDate(LocalDateTime.now().minusDays(2));
        h2.setDescription("Test block");
        historyRepository.save(h2);
    }

    @Test
    void filterHistory_byEventType() throws Exception {
        mockMvc.perform(get("/api/history")
                .param("eventType", "TRANSFER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventType").value("TRANSFER"));
    }

    @Test
    void filterHistory_byDateRange() throws Exception {
        String from = LocalDateTime.now().minusDays(3).toString();
        String to = LocalDateTime.now().toString();
        mockMvc.perform(get("/api/history")
                .param("dateFrom", from)
                .param("dateTo", to)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void filterHistory_pagination() throws Exception {
        mockMvc.perform(get("/api/history")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
